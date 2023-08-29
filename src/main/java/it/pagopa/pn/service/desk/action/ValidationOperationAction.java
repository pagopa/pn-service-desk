package it.pagopa.pn.service.desk.action;

import it.pagopa.pn.service.desk.action.common.BaseAction;
import it.pagopa.pn.service.desk.exception.ExceptionTypeEnum;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.exception.PnRetryStorageException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.ResponsePaperNotificationFailedDtoDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.service.desk.mapper.AttachmentMapper;
import it.pagopa.pn.service.desk.mapper.PaperRequestMapper;
import it.pagopa.pn.service.desk.middleware.db.dao.AddressDAO;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.addressmanager.PnAddressManagerClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.paperchannel.PnPaperChannelClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.safestorage.PnSafeStorageClient;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import it.pagopa.pn.service.desk.utility.Utility;
import lombok.CustomLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.utils.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ADDRESS_IS_NOT_VALID;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

@Slf4j
@Qualifier("ValidationAction")
@Component
@CustomLog
public class ValidationOperationAction implements BaseAction<String> {

    @Autowired
    private OperationDAO operationDAO;

    @Autowired
    private AddressDAO addressDAO;

    @Autowired
    private PnAddressManagerClient addressManagerClient;

    @Autowired
    private PnDeliveryPushClient pnDeliveryPushClient;

    @Autowired
    private PnDeliveryClient pnDeliveryClient;

    @Autowired
    private PnPaperChannelClient paperChannelClient;

    @Autowired
    private PnSafeStorageClient safeStorageClient;

    @Override
    public void execute(String operationId){
        operationDAO.getByOperationId(operationId)
                .zipWhen(operations ->
                        getAddressFromOperationId(operationId)
                ).map(operationAndAddress ->
                        getIuns(operationAndAddress.getT1().getRecipientInternalId())
                            .collectList()
                            .doOnNext(responsePaperNotificationFailed -> updateOperationStatus(operationAndAddress.getT1(), OperationStatusEnum.VALIDATION))
                            .flatMapMany(Flux::fromIterable)
                            .parallel()
                            .flatMap(iun -> getAttachmentsFromIun(operationAndAddress.getT1(), iun))
                            .sequential()
                            .flatMap(pnServiceDeskAttachments -> Flux.fromIterable(pnServiceDeskAttachments.getFilesKey()))
                            .collectList()
                            .flatMap(attachments -> paperPrepare(operationAndAddress.getT1(), operationAndAddress.getT2(), attachments))

                ).block();
    }

    /**
     * Retrieve address from AddressDAO
     * @param operationId id of operation
     * @return Address from DB
     */
    private Mono<PnServiceDeskAddress> getAddressFromOperationId(String operationId){
        return addressDAO.getAddress(operationId)
                .doOnSuccess(this::validationAddress);
    }

    /**
     * Validation address from AddressManagerClient
     * @param address  address's operation
     * @throws it.pagopa.pn.service.desk.exception.PnGenericException only if address is not valid
     */
    private Mono<Void> validationAddress(PnServiceDeskAddress address){
        return addressManagerClient.deduplicates(address)
                .flatMap(deduplicateResponse -> {
                    if (FALSE.equals(deduplicateResponse.getEqualityResult())) {
                        return Mono.error(new PnGenericException(ADDRESS_IS_NOT_VALID, ADDRESS_IS_NOT_VALID.getMessage()));
                    }
                    if (StringUtils.isNotBlank(deduplicateResponse.getError())){
                        return Mono.error(new PnGenericException(ADDRESS_IS_NOT_VALID, ADDRESS_IS_NOT_VALID.getMessage()));
                    }
                    return Mono.just(deduplicateResponse);
                }).then();
    }





    private Mono<PnServiceDeskAttachments> getAttachmentsFromIun(PnServiceDeskOperations operation, String iun){
        return Mono.just(AttachmentMapper.initAttachment(iun))
                .flatMap(entity ->
                        this.getAttachmentsFromDelivery(iun).concatWith(getAttachmentsFromDeliveryPush(operation.getRecipientInternalId(), iun))
                                .doOnNext(fileKey -> {
                                    if (TRUE.equals(entity.getIsAvailable())){
                                        isFileAvailable(fileKey)
                                                .doOnSuccess(isAvailable ->{
                                                    log.debug("Document is available ? {}", isAvailable);
                                                    entity.setIsAvailable(entity.getIsAvailable() && isAvailable);
                                                });
                                    }
                                })
                                .collectList()
                                .map(fileKeys -> {
                                    entity.setFilesKey(fileKeys);
                                    return entity;
                                })
                ).flatMap(entity -> {
                    operation.getAttachments().add(entity);
                    return operationDAO.updateEntity(operation).map(item -> entity);
                });
    }

    private Mono<Boolean> isFileAvailable(String fileKey){
        return this.getFileRecursive(5, fileKey, BigDecimal.ZERO)
                //TODO aggiungere chiamata per il download del documento
                .map(response -> TRUE)
                .onErrorResume(ex -> Mono.just(FALSE));
    }


    /**
     * retrieve file keys from deliveryPush attachments
     * @param recipientInternalId
     * @param iun
     * @return only fileKeys
     */
    private Flux<String> getAttachmentsFromDeliveryPush(String recipientInternalId, String iun){
        return  pnDeliveryPushClient.getNotificationLegalFactsPrivate(recipientInternalId, iun)
                .parallel()
                .map(legalFact -> legalFact.getLegalFactsId().getKey())
                .sequential();
    }

    /**
     * retrieve file keys from delivery attachments
     * @param iun
     * @return only fileKeys
     */
    private Flux<String> getAttachmentsFromDelivery(String iun){
        return pnDeliveryClient.getSentNotificationPrivate(iun)
                .flatMapMany(doc -> Flux.fromIterable(doc.getDocuments()))
                .parallel()
                .map(item -> item.getRef().getKey())
                .sequential();
    }

    private Mono<Void> updateOperationStatus(PnServiceDeskOperations operations, OperationStatusEnum operationStatusEnum){
        operations.setStatus(operationStatusEnum.toString());
        return this.operationDAO.updateEntity(operations).then();
    }

    private Mono<Void> paperPrepare(PnServiceDeskOperations operations, PnServiceDeskAddress address, List<String> attachments){
        String requestId = Utility.generateRequestId(operations.getOperationId());
        return paperChannelClient.sendPaperPrepareRequest(requestId, PaperRequestMapper.getPrepareRequest(operations,address, attachments, requestId))
                .doOnSuccess(response -> updateOperationStatus(operations, OperationStatusEnum.PREPARING))
                .then();
    }


    private Flux<String> getIuns(String recipientInternalId){
        return pnDeliveryPushClient.paperNotificationFailed(recipientInternalId)
                .parallel()
                .map(ResponsePaperNotificationFailedDtoDto::getIun)
                .sequential();
    }

    private Mono<FileDownloadResponse> getFileRecursive(Integer n, String fileKey, BigDecimal millis){
        if (n<0)
            return Mono.error(new PnGenericException( ExceptionTypeEnum.DOCUMENT_URL_NOT_FOUND, ExceptionTypeEnum.DOCUMENT_URL_NOT_FOUND.getMessage() ) );
        else {
            return Mono.delay(Duration.ofMillis( millis.longValue() ))
                    .flatMap(item -> safeStorageClient.getFile(fileKey)
                            .map(fileDownloadResponseDto -> fileDownloadResponseDto)
                            .onErrorResume(ex -> {
                                log.error("Error with retrieve {}", ex.getMessage());
                                return Mono.error(ex);
                            })
                            .onErrorResume(PnRetryStorageException.class, ex ->
                                    getFileRecursive(n - 1, fileKey, ex.getRetryAfter())
                            ));
        }
    }

}
