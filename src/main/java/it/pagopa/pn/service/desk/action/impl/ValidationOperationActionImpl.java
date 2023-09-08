package it.pagopa.pn.service.desk.action.impl;

import it.pagopa.pn.service.desk.action.ValidationOperationAction;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.exception.ExceptionTypeEnum;
import it.pagopa.pn.service.desk.exception.PnEntityNotFoundException;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.exception.PnRetryStorageException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.ResponsePaperNotificationFailedDtoDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.service.desk.mapper.AttachmentMapper;
import it.pagopa.pn.service.desk.mapper.PaperChannelMapper;
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
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.utils.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ADDRESS_IS_NOT_VALID;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ADDRESS_IS_NOT_PRESENT;
import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_ON_DELIVERY_CLIENT;
import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_ON_UPDATE_ETITY;
import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_ON_DELIVERY_PUSH_CLIENT;
import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_ON_ADDRESS_MANAGER_CLIENT;
import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_ON_SEND_PAPER_CHANNEL_CLIENT;



@Component
@CustomLog
@AllArgsConstructor
public class ValidationOperationActionImpl implements ValidationOperationAction {

    private OperationDAO operationDAO;
    private AddressDAO addressDAO;
    private PnAddressManagerClient addressManagerClient;
    private PnDeliveryPushClient pnDeliveryPushClient;
    private PnDeliveryClient pnDeliveryClient;
    private PnPaperChannelClient paperChannelClient;
    private PnSafeStorageClient safeStorageClient;
    private PnServiceDeskConfigs cfn;

    @Override
    public void execute(String operationId){
        operationDAO.getByOperationId(operationId)
                .switchIfEmpty(Mono.error(new PnEntityNotFoundException()))
                .doOnNext(operation -> log.debug("Operation retrieved {}", operation))
                .zipWhen(operation -> getAddressFromOperationId(operationId))
                .doOnNext(operationAndAddress -> log.debug("Start retrieve iuns"))
                .flatMap(operationAndAddress ->
                        getIuns(operationAndAddress.getT1().getRecipientInternalId())
                                .collectList()
                                .doOnNext(responsePaperNotificationFailed -> {
                                    operationAndAddress.getT1().setErrorReason(null);
                                    updateOperationStatus(operationAndAddress.getT1(), OperationStatusEnum.VALIDATION);
                                })
                                .flatMapMany(Flux::fromIterable)
                                .parallel()
                                .flatMap(iun -> getAttachmentsFromIun(operationAndAddress.getT1(), iun))
                                .sequential()
                                .flatMap(pnServiceDeskAttachments -> {
                                    if (Boolean.TRUE.equals(pnServiceDeskAttachments.getIsAvailable())){
                                        return Flux.fromIterable(pnServiceDeskAttachments.getFilesKey());
                                    }
                                    return Flux.empty();
                                })
                                .collectList()
                                .flatMap(attachments -> paperPrepare(operationAndAddress.getT1(), operationAndAddress.getT2(), attachments))
                                .doOnError(ex -> log.error("ERROR VALIDATION ", ex))

                )
                .doOnError(PnEntityNotFoundException.class, error -> log.error("The operation entity was not found with this operationId: {}", operationId))
                .onErrorResume(ex -> {
                    if (ex instanceof PnEntityNotFoundException) {
                        return Mono.error(ex);
                    }
                    return traceErrorOnDB(operationId, ex);
                })
                .block();
    }

    /**
     * Retrieve address from AddressDAO
     * @param operationId id of operation
     * @return Address from DB
     */
    private Mono<PnServiceDeskAddress> getAddressFromOperationId(String operationId){
        return addressDAO.getAddress(operationId)
                .switchIfEmpty(Mono.error(new PnGenericException(ADDRESS_IS_NOT_PRESENT, ADDRESS_IS_NOT_PRESENT.getMessage(), HttpStatus.BAD_REQUEST)))
                .doOnNext(address ->  log.debug("Address retrieved {}", address))
                .flatMap(response -> validationAddress(response).thenReturn(response));
    }

    /**
     * Validation address from AddressManagerClient
     * @param address  address's operation
     * @throws it.pagopa.pn.service.desk.exception.PnGenericException only if address is not valid
     */
    private Mono<Void> validationAddress(PnServiceDeskAddress address){
        return addressManagerClient.deduplicates(address)
                .onErrorResume(ex -> Mono.error(new PnGenericException(ERROR_ON_ADDRESS_MANAGER_CLIENT,  ex.getMessage(), HttpStatus.BAD_REQUEST)))
                .doOnNext( deduplicatesResponseDto ->  log.debug("Address deduplicates {}", deduplicatesResponseDto))
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
                ).zipWith(Mono.just(operation))
                .flatMap(attachmentAndOperation -> {
                    PnServiceDeskOperations deskOperations = attachmentAndOperation.getT2();
                    if (deskOperations.getAttachments() == null){
                        deskOperations.setAttachments(new ArrayList<>());
                    }
                    deskOperations.getAttachments().add(attachmentAndOperation.getT1());
                    log.debug("Added attachments in operation {}", deskOperations.getAttachments().size());
                    return operationDAO.updateEntity(deskOperations)
                            .switchIfEmpty(Mono.error(new PnGenericException(ERROR_ON_UPDATE_ETITY, ERROR_ON_UPDATE_ETITY.getMessage(), HttpStatus.BAD_REQUEST)))
                            .thenReturn(attachmentAndOperation.getT1());
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
                .onErrorResume(ex -> Mono.error(new PnGenericException(ERROR_ON_DELIVERY_PUSH_CLIENT, ex.getMessage(), HttpStatus.BAD_REQUEST)))
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
                .onErrorResume(ex -> Mono.error(new PnGenericException(ERROR_ON_DELIVERY_CLIENT, ex.getMessage(), HttpStatus.BAD_REQUEST)))
                .flatMapMany(doc -> Flux.fromIterable(doc.getDocuments()))
                .parallel()
                .map(item -> item.getRef().getKey())
                .sequential();
    }

    private Mono<Void> updateOperationStatus(PnServiceDeskOperations operations, OperationStatusEnum operationStatusEnum){
        operations.setStatus(operationStatusEnum.toString());
        return this.operationDAO.updateEntity(operations)
                .switchIfEmpty(Mono.error(new PnGenericException(ERROR_ON_UPDATE_ETITY, ERROR_ON_UPDATE_ETITY.getMessage(), HttpStatus.BAD_REQUEST)))
                .doOnNext( operation ->  log.debug("Update  operationsStatus {}", operationStatusEnum))
                .then();
    }

    private Mono<Void> paperPrepare(PnServiceDeskOperations operations, PnServiceDeskAddress address, List<String> attachments){
        String requestId = Utility.generateRequestId(operations.getOperationId());
        return paperChannelClient.sendPaperPrepareRequest(requestId, PaperChannelMapper.getPrepareRequest(operations,address, attachments, requestId, cfn))
                .onErrorResume(ex -> Mono.error(new PnGenericException(ERROR_ON_SEND_PAPER_CHANNEL_CLIENT, ex.getMessage(), HttpStatus.BAD_REQUEST)))
                .doOnNext(response -> log.debug("Sent paper prepare  {}", response))
                .flatMap(response -> {
                    operations.setErrorReason(null);
                    return updateOperationStatus(operations, OperationStatusEnum.PREPARING);
                })
                .then();
    }


    private Flux<String> getIuns(String recipientInternalId){
        return pnDeliveryPushClient.paperNotificationFailed(recipientInternalId)
                .onErrorResume(ex -> Mono.error(new PnGenericException(ERROR_ON_DELIVERY_PUSH_CLIENT, ex.getMessage(), HttpStatus.BAD_REQUEST)))
                .doOnNext(iun -> log.debug("IUN : {}", iun))
                .map(ResponsePaperNotificationFailedDtoDto::getIun);
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

    private Mono<Void> traceErrorOnDB(String operationId, Throwable ex) {
        log.error("Error on validation flow {}", ex.getMessage(), ex);
        return operationDAO.getByOperationId(operationId)
                .flatMap(operation -> {
                    operation.setErrorReason(ex.getMessage());
                    return updateOperationStatus(operation, OperationStatusEnum.KO);
                });
    }


}
