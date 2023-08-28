package it.pagopa.pn.service.desk.action;

import it.pagopa.pn.service.desk.exception.ExceptionTypeEnum;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.exception.PnRetryStorageException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.ResponsePaperNotificationFailedDtoDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
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
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.safestorage.dto.FileDownloadResponseDto;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import it.pagopa.pn.service.desk.utility.Utility;
import lombok.CustomLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ADDRESS_IS_NOT_VALID;

@Component
@CustomLog
public class ValidationOperationAction {

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

    public void validateOperation(String operationId){
        operationDAO.getByOperationId(operationId)
                .zipWhen(operations ->
                        getAddressFromOperationId(operationId)
                ).map(operationAndAddress ->
                        getIuns(operationAndAddress.getT1().getRecipientInternalId())
                            .collectList()
                            .doOnNext(responsePaperNotificationFailed -> updateStatus(operationAndAddress.getT1(), OperationStatusEnum.VALIDATION))
                            .flatMapMany(Flux::fromIterable)
                            .parallel()
                            .flatMap(iun -> getNotificationsAttachments(operationAndAddress.getT1(), iun))
                            .sequential()
                            .flatMap(pnServiceDeskAttachments -> Flux.fromIterable(pnServiceDeskAttachments.getFilesKey()))
                            .collectList()
                            .flatMap(attachments -> paperPrepare(operationAndAddress.getT1(), operationAndAddress.getT2(), attachments))

                ).block();

            // per ogni iun recuperato da deliveryPush ->
            // getNotificationsAttachments(iun);
        // update pnServiceDeskOperation aggiungendo la lista dei PnServiceDeskAttachments
        //Utility.GenerateRequestId(operationId)
        // chiamare la prepare di PaperChannel -> PnPaperChannelClient.prepare()
        // se chiamata di prepare ritorna 201 -> update pnServiceDeskOperation con status = PREPARING
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
                    if (Boolean.FALSE.equals(deduplicateResponse.getEqualityResult())) {
                        return Mono.error(new PnGenericException(ADDRESS_IS_NOT_VALID, ADDRESS_IS_NOT_VALID.getMessage()));
                    }
                    if (StringUtils.isNotBlank(deduplicateResponse.getError())){
                        return Mono.error(new PnGenericException(ADDRESS_IS_NOT_VALID, ADDRESS_IS_NOT_VALID.getMessage()));
                    }
                    return Mono.just(deduplicateResponse);
                }).then();
    }





    private Mono<Void> getNotificationsAttachments(PnServiceDeskOperations operation, String iun){

        PnServiceDeskAttachments pnServiceDeskAttachments = new PnServiceDeskAttachments();
        pnServiceDeskAttachments.setIun(iun);
        pnServiceDeskAttachments.setIsAvailable(Boolean.TRUE);
        pnServiceDeskAttachments.setFilesKey(new ArrayList<>());

        return Mono.just(pnServiceDeskAttachments)
                .doOnNext(entity ->
                        this.getAttachmentsFromDelivery(iun).concatWith(getAttachmentsFromDeliveryPush(operation.getRecipientInternalId(), iun))
                                .doOnNext(fileKey -> {
                                    entity.getFilesKey().add(fileKey);
                                    if (Boolean.TRUE.equals(pnServiceDeskAttachments.getIsAvailable())){
                                        getFile(fileKey)
                                                .doOnSuccess(isAvailable -> {
                                                    pnServiceDeskAttachments.setIsAvailable(pnServiceDeskAttachments.getIsAvailable() && isAvailable);
                                                });
                                    }
                                })
                ).doOnSuccess(entity -> {
                    operation.getAttachments().add(entity);
                    operationDAO.updateEntity(operation);
                })
                .then();
    }

    private Mono<Boolean> getFile(String fileKey){
        // this.recursive(....)
        // .map(response -> return TRUE)
        // .onErrorResume(ex -> return Mono.just(FALSE)
        return null;
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

    private Mono<PnServiceDeskOperations> updateStatus (PnServiceDeskOperations operations, OperationStatusEnum operationStatusEnum){
        operations.setStatus(operationStatusEnum.toString());
        return this.operationDAO.updateEntity(operations);
    }

    private Mono<Void> paperPrepare (PnServiceDeskOperations operations, PnServiceDeskAddress address, List<String> attachments){
        String requestId = Utility.generateRequestId(operations.getOperationId());
        return paperChannelClient.sendPaperPrepareRequest(requestId, PaperRequestMapper.getPrepareRequest(operations,address, attachments, requestId))
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
                                log.error ("Error with retrieve {}", ex.getMessage());
                                return Mono.error(ex);
                            })
                            .onErrorResume(PnRetryStorageException.class, ex ->
                                    getFileRecursive(n - 1, fileKey, ex.getRetryAfter())
                            ));
        }
    }

}
