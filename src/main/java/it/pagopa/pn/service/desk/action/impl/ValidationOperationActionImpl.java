package it.pagopa.pn.service.desk.action.impl;

import it.pagopa.pn.service.desk.action.ValidationOperationAction;
import it.pagopa.pn.service.desk.config.HttpConnector;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.exception.ExceptionTypeEnum;
import it.pagopa.pn.service.desk.exception.PnEntityNotFoundException;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.exception.PnRetryStorageException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.ResponsePaperNotificationFailedDtoDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.PaperChannelUpdateDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.service.desk.mapper.AttachmentMapper;
import it.pagopa.pn.service.desk.mapper.OperationMapper;
import it.pagopa.pn.service.desk.mapper.PaperChannelMapper;
import it.pagopa.pn.service.desk.middleware.db.dao.AddressDAO;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.addressmanager.PnAddressManagerClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault.PnDataVaultClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.paperchannel.PnPaperChannelClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.safestorage.PnSafeStorageClient;
import it.pagopa.pn.service.desk.model.AttachmentInfo;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import it.pagopa.pn.service.desk.service.impl.BaseService;
import it.pagopa.pn.service.desk.utility.Utility;
import lombok.CustomLog;
import org.apache.http.client.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

@Component
@CustomLog
public class ValidationOperationActionImpl extends BaseService implements ValidationOperationAction {

    private AddressDAO addressDAO;
    private PnAddressManagerClient addressManagerClient;
    private PnDeliveryPushClient pnDeliveryPushClient;
    private PnDeliveryClient pnDeliveryClient;
    private PnPaperChannelClient paperChannelClient;
    private PnSafeStorageClient safeStorageClient;
    private PnServiceDeskConfigs cfn;
    private PnDataVaultClient pnDataVaultClient;

    public ValidationOperationActionImpl(OperationDAO operationDao, AddressDAO addressDAO,
                                         PnAddressManagerClient addressManagerClient, PnDeliveryPushClient pnDeliveryPushClient,
                                         PnDeliveryClient pnDeliveryClient,
                                         PnPaperChannelClient paperChannelClient, PnSafeStorageClient safeStorageClient,
                                         PnServiceDeskConfigs cfn, PnDataVaultClient pnDataVaultClient) {
        super(operationDao);
        this.addressDAO = addressDAO;
        this.addressManagerClient = addressManagerClient;
        this.pnDeliveryClient = pnDeliveryClient;
        this.pnDeliveryPushClient = pnDeliveryPushClient;
        this.paperChannelClient = paperChannelClient;
        this.safeStorageClient = safeStorageClient;
        this.cfn = cfn;
        this.pnDataVaultClient = pnDataVaultClient;
    }

    @Override
    public void execute(String operationId) {
        log.debug("operationId = {}, ValidationOperationAction - Execute received input", operationId);

        log.debug("operationId = {}, Retrieving entityOperation from Database", operationId);
        operationDAO.getByOperationId(operationId)
                .switchIfEmpty(Mono.error(new PnEntityNotFoundException()))
                .doOnNext(operation -> log.debug("operationId = {}, operation = {}, EntityOperation retrieved", operationId, operation))
                .zipWhen(operation -> {
                    log.debug("operationId = {}, operation = {}, Retrieving address from operationId", operationId, operation);
                    return getAddressFromOperationId(operationId);
                })
                .doOnNext(operationAndAddress ->
                        log.debug("operation = {}, address = {} Address retrivied", operationAndAddress.getT1(), operationAndAddress.getT2()))
                .flatMap(operationAndAddress -> checkValidationFlow(operationAndAddress.getT1(), operationAndAddress.getT2()))
                .doOnError(PnEntityNotFoundException.class, error -> log.error("operationId = {}, Operation entity was not found", operationId))
                .onErrorResume(ex -> {
                    if (ex instanceof PnEntityNotFoundException) {
                        return Mono.error(ex);
                    }
                    return traceErrorOnDB(operationId, ex);
                })
                .block();
    }

    private Mono<Void> checkValidationFlow(PnServiceDeskOperations operation, PnServiceDeskAddress address) {
        return getIuns(operation.getRecipientInternalId())
                .collectList()
                .flatMap(responsePaperNotificationFailed -> {
                    if (responsePaperNotificationFailed.isEmpty()) {
                        return Mono.error(new PnGenericException(IUNS_ALREADY_IN_PROGRESS, IUNS_ALREADY_IN_PROGRESS.getMessage()));
                    }

                    log.debug("listOfIuns = {}, List of iuns retrivied", responsePaperNotificationFailed);
                    operation.setErrorReason(null);
                    log.debug("errorReason = {}, Setting to null errorReason into entityOperation", operation);
                    return updateOperationStatus(operation, OperationStatusEnum.VALIDATION).thenReturn(responsePaperNotificationFailed);
                })
                .flatMapMany(Flux::fromIterable)
                .flatMap(iun -> {
                    log.debug("iun = {}, Get attachment from iun", iun);
                    return getAttachmentsFromIun(operation, iun);
                })
                .collectList()
                .flatMap(attachments -> splitAttachments(attachments)
                        .collectList()
                        .map(lists -> lists))
                .doOnNext(pnServiceDeskAttachmentsList -> {
                    log.debug("entityOperation = {}, Is entityOperation's attachments null?", operation);
                    if (operation.getAttachments() == null){
                        log.debug("entityOperation = {}, A new entityOperation's attachments list has been created", operation);
                        operation.setAttachments(new ArrayList<>());
                    }
                    operation.getAttachments().addAll(pnServiceDeskAttachmentsList);
                })
                .flatMap(pnServiceDeskAttachments -> {
                    log.debug("entityOperation = {}, entityAttachment = {}, Entity's attachment list has been added", operation, pnServiceDeskAttachments);
                    return operationDAO.updateEntity(operation)
                            .switchIfEmpty(Mono.defer(() -> {
                                log.error("entityOperation = {}, Error on update entityOperation", operation);
                                return Mono.error(new PnGenericException(ERROR_ON_UPDATE_ENTITY, ERROR_ON_UPDATE_ENTITY.getMessage()));
                            }))
                            .thenReturn(pnServiceDeskAttachments);
                })
                .flatMapMany(Flux::fromIterable)
                .flatMap(this::getFileKeyFromAttachments)
                .collectList()
                .flatMap(attachments -> {
                    log.debug("entityOperation = {}, entityAddress = {}, attachments = {}, All data requirements are available to make the call to prepare", operation, address, attachments);
                    return paperPrepare(operation, address, attachments);
                })
                .doOnError(exception -> log.error("errorReason = {}, Error during the validation flow", exception.getMessage()));
    }

    private Flux<String> getFileKeyFromAttachments(PnServiceDeskAttachments pnServiceDeskAttachments) {
        log.debug("entityAttachments = {}, iun = {}, Are attachments available for this iun?", pnServiceDeskAttachments, pnServiceDeskAttachments.getIun());
        if (Boolean.TRUE.equals(pnServiceDeskAttachments.getIsAvailable())){
            log.debug("entityAttachments = {}, iun = {}, Attachments are available for this iun", pnServiceDeskAttachments, pnServiceDeskAttachments.getIun());
            return Flux.fromIterable(pnServiceDeskAttachments.getFilesKey());
        }
        log.debug("entityAttachments = {}, iun = {}, Attachments are not available for this iun", pnServiceDeskAttachments, pnServiceDeskAttachments.getIun());
        return Flux.empty();
    }

    /**
     * Retrieve address from AddressDAO
     * @param operationId id of operation
     * @return Address from DB
     */
    private Mono<PnServiceDeskAddress> getAddressFromOperationId(String operationId) {
        log.debug("operationId: {}, GetAddressFromOperationId received input", operationId);

        log.debug("operationId: {}, Retrieving address associated to operationId", operationId);
        return addressDAO.getAddress(operationId)
                .switchIfEmpty(Mono.defer(() -> {
                        log.error("operationId = {}, EntityAddress was not found", operationId);
                        return Mono.error(new PnGenericException(ADDRESS_IS_NOT_PRESENT, ADDRESS_IS_NOT_PRESENT.getMessage()));
                }))
                .doOnNext(address -> log.debug("operationId = {}, Address retrieved with success {}", operationId, address))
                .flatMap(response -> {
                    log.debug("operationId = {}, Ready for validation the retrieved address", operationId);
                    return validationAddress(response).thenReturn(response);
                })
                .doOnError(PnGenericException.class, exception -> log.error("operationId = {}, errorReason = {}, Address associated with the operation was not recovered", operationId, exception.getMessage()));
    }

    /**
     * Validation address from AddressManagerClient
     * @param address  address's operation
     * @throws it.pagopa.pn.service.desk.exception.PnGenericException only if address is not valid
     */
    private Mono<Void> validationAddress(PnServiceDeskAddress address) {
        log.debug("address: {}, ValidationAddress received input", address);

        return addressManagerClient.deduplicates(address)
                .onErrorResume(exception -> {
                    log.error("errorReason = {}, Error during ", exception.getMessage());
                    return Mono.error(new PnGenericException(ERROR_ON_ADDRESS_MANAGER_CLIENT,  exception.getMessage()));
                })
                .doOnNext(deduplicatesResponseDto ->  log.debug("deduplicatesResponseDto = {}, Address is validated by Address manager service", deduplicatesResponseDto))
                .flatMap(deduplicateResponse -> {
                    log.debug("deduplicateResponse = {}, Address response", deduplicateResponse);
                    log.debug("equalityResult = {}, Is Equality result false?", deduplicateResponse.getEqualityResult());
                    if (FALSE.equals(deduplicateResponse.getEqualityResult())) {
                        log.error("deduplicateResponse = {}, Address response", deduplicateResponse);
                        return Mono.error(new PnGenericException(ADDRESS_IS_NOT_VALID, ADDRESS_IS_NOT_VALID.getMessage()));
                    }
                    log.debug("equalityError = {}, Is a valid address?", deduplicateResponse.getError());
                    if (StringUtils.isNotBlank(deduplicateResponse.getError())){
                        log.error("errorReason = {}, The address is not valid", deduplicateResponse);
                        return Mono.error(new PnGenericException(ADDRESS_IS_NOT_VALID, ADDRESS_IS_NOT_VALID.getMessage()));
                    }
                    log.debug("deduplicateResponse = {}, The address is valid", deduplicateResponse);
                    return Mono.just(deduplicateResponse);
                }).then();
    }

    private Mono<PnServiceDeskAttachments> getAttachmentsFromIun(PnServiceDeskOperations entityOperation, String iun) {
        log.debug("entityOperation: {}, iun: {}, GetAttachmentsFromIun received input", entityOperation, iun);

        return Mono.just(AttachmentMapper.initAttachment(iun))
                .flatMap(entity ->
                    this.getAttachmentsFromDelivery(iun)
                            .concatWith(getAttachmentsFromDeliveryPush(entityOperation.getRecipientInternalId(), iun))
                            .flatMap(fileKey -> {
                                log.debug("fileKey = {}, Is the file received on available?", fileKey);
                                if (TRUE.equals(entity.getIsAvailable())){
                                    return attachmentInfo(fileKey)
                                            .map(attachmentInfo ->{
                                                log.debug("fileKey = {}, isAvailable = {}, The file received is available", fileKey, attachmentInfo.isAvailable());
                                                entity.setIsAvailable(entity.getIsAvailable() && attachmentInfo.isAvailable());
                                                entity.setNumberOfPages(attachmentInfo.getNumberOfPage());
                                                return fileKey.contains(Utility.SAFESTORAGE_BASE_URL) ? fileKey : Utility.SAFESTORAGE_BASE_URL.concat(fileKey);
                                            });
                                }
                                return Mono.just(fileKey);
                            })
                            .collectList()
                            .map(fileKeys -> {
                                entity.setFilesKey(fileKeys);
                                log.debug("fileKeys = {}, entityAttachment = {}, EntityAttachment's list of filesKey has been setted", fileKeys, entity);
                                return entity;
                            }));
    }

    private Mono<AttachmentInfo> attachmentInfo(String fileKey) {
        log.debug("fileKey: {}, IsFileAvailable received input", fileKey);

        return this.getFileRecursive(5, fileKey, BigDecimal.ZERO)
                .flatMap(response -> {
                    AttachmentInfo info = AttachmentMapper.fromSafeStorage(response);
                    if (info.getUrl() == null){
                        info.setAvailable(FALSE);
                        return Mono.just(info);
                    }
                    return HttpConnector.downloadFile(info.getUrl())
                            .map(pdDocument -> {
                                try {
                                    info.setAvailable(TRUE);
                                    info.setNumberOfPage(pdDocument.getNumberOfPages());
                                    pdDocument.close();
                                } catch (IOException e) {
                                    throw new PnGenericException(ERROR_SAFE_STORAGE_BODY_NULL, ERROR_SAFE_STORAGE_BODY_NULL.getMessage());
                                }
                                return info;
                            });

                })
                .onErrorResume(exception -> {
                    log.debug("errorReason = {}, An error occurred while retrieving the file", exception.getMessage());
                    AttachmentInfo info = new AttachmentInfo();
                    info.setAvailable(FALSE);
                    return Mono.just(info);
                });
    }


    /**
     * retrieve file keys from deliveryPush attachments
     * @param recipientInternalId
     * @param iun
     * @return only fileKeys
     */
    private Flux<String> getAttachmentsFromDeliveryPush(String recipientInternalId, String iun) {
        log.debug("recipientInternalId: {}, iun: {}, GetAttachmentsFromDeliveryPush received input", recipientInternalId, iun);

        log.debug("iun = {}, recipientInternalId = {}, Calling DeliveryPush's legalFacts service", iun, recipientInternalId);
        return  pnDeliveryPushClient.getNotificationLegalFactsPrivate(recipientInternalId, iun)
                .onErrorResume(exception -> {
                    log.error("errorReason = {}, An error occurred while call service legalFacts", exception.getMessage());
                    return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_PUSH_CLIENT, exception.getMessage()));
                })
                .collectList()
                .zipWith(pnDataVaultClient.deAnonymized(recipientInternalId))
                .flatMapMany(legalFactAndTaxId ->  Flux.fromIterable(
                        legalFactAndTaxId.getT1().stream()
                                .filter(legalFact -> (StringUtils.isEmpty(legalFact.getTaxId())  || legalFact.getTaxId().equalsIgnoreCase(legalFactAndTaxId.getT2())))
                                .map(l -> l.getLegalFactsId().getKey())
                                .toList()));
    }

    /**
     * retrieve file keys from delivery attachments
     * @param iun
     * @return only fileKeys
     */
    private Flux<String> getAttachmentsFromDelivery(String iun) {
        log.debug("iun: {}, GetAttachmentsFromDelivery received input", iun);

        log.debug("iun = {}, Calling service to obtain notification sent", iun);
        return pnDeliveryClient.getSentNotificationPrivate(iun)
                .onErrorResume(exception -> {
                    log.error("errorReason = {}, An error occurred while call service for obtain notification sent", exception.getMessage());
                    return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_CLIENT, exception.getMessage()));
                })
                .flatMapMany(doc -> Flux.fromIterable(doc.getDocuments()))
                .map(notificationDocument -> {
                    log.debug("notificationDocument = {}, Notification received with success", notificationDocument);
                    return notificationDocument.getRef().getKey();
                });
    }

    private Mono<Void> updateOperationStatus(PnServiceDeskOperations entityOperation, OperationStatusEnum operationStatusEnum) {
        log.debug("entityOperation = {}, operationStatus = {}, UpdateOperationStatus received input", entityOperation, operationStatusEnum);

        log.debug("entityOperation = {}, operationStatus = {}, Update entityOperation with new status", entityOperation, operationStatusEnum.toString());
        entityOperation.setStatus(operationStatusEnum.toString());
        return this.operationDAO.updateEntity(entityOperation)
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("entityOperation = {}, operationStatus = {}, Error on update entityOperation", entityOperation, operationStatusEnum);
                    return Mono.error(new PnGenericException(ERROR_ON_UPDATE_ENTITY, ERROR_ON_UPDATE_ENTITY.getMessage()));
                }))
                .doOnNext(operation ->  log.debug("entityOperation = {}, operationsStatus {}, EntityOperation updated with new status", entityOperation, operationStatusEnum))
                .then();
    }

    private Mono<Void> paperPrepare(PnServiceDeskOperations entityOperation, PnServiceDeskAddress address, List<String> attachments) {
        log.debug("entityOperation = {}, entityAddress = {}, PaperPrepare received input", entityOperation, address);

        String requestId = Utility.generateRequestId(entityOperation.getOperationId());
        log.debug("requestId = {}, Generated a new requestId", requestId);

        log.debug("entityOperation = {}, Is attachments null or empty?", entityOperation);
        if (attachments == null || attachments.isEmpty()) {
            log.error("entityOperation = {}, Attachments list is null, there are no attachments available for this operation", entityOperation);

            String errorMessage = NO_ATTACHMENT_AVAILABLE.getMessage()
                    .concat(entityOperation.getOperationId())
                    .concat(" - ")
                    .concat(requestId);
            log.debug(errorMessage);
            throw new PnGenericException(NO_ATTACHMENT_AVAILABLE, errorMessage);
        }

        log.debug("recipientInternalId = {}, Calling service for deanonymizing this recipientInternalId", entityOperation.getRecipientInternalId());
        return this.pnDataVaultClient.deAnonymized(entityOperation.getRecipientInternalId())
                .map(fiscalCode -> PaperChannelMapper.getPrepareRequest(entityOperation, address, attachments, requestId, fiscalCode, cfn))
                .flatMap(prepareRequestDto -> {
                    log.debug("requestId = {}, prepareRequestDto = {}, Calling prepare api with this requestId and request", requestId, prepareRequestDto);
                    return this.paperChannelClient.sendPaperPrepareRequest(requestId, prepareRequestDto);
                })
                .switchIfEmpty(Mono.just(new PaperChannelUpdateDto()))
                .doOnNext(response -> log.debug("response = {}, Paper prepare request has been sent", response))
                .flatMap(response -> {
                    entityOperation.setErrorReason(null);
                    log.debug("errorReason = {}, Setting to null errorReason into entityOperation", entityOperation);
                    return updateOperationStatus(entityOperation, OperationStatusEnum.PREPARING);
                })
                .onErrorResume(exception -> {
                    log.error("requestId = {}, errorReason = {}, Error during call paperchannel prepare", requestId, exception.getMessage());
                    return Mono.error(new PnGenericException(ERROR_ON_SEND_PAPER_CHANNEL_CLIENT, exception.getMessage()));
                })
                .then();
    }


    private Flux<String> getIuns(String recipientInternalId) {
        log.debug("recipientInternalId = {}, GetIuns received input", recipientInternalId);

        return pnDeliveryPushClient.paperNotificationFailed(recipientInternalId)
                .onErrorResume(ex -> {
                    log.error("recipientInternalId = {}, errorReason = {}, Error on delivery push client", recipientInternalId, ex.getMessage());
                    return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_PUSH_CLIENT, ex.getMessage()));
                })
                .doOnNext(iun -> log.debug("recipientInternalId = {}, iun = {}, Iun retrievied", iun, recipientInternalId))
                .map(ResponsePaperNotificationFailedDtoDto::getIun)
                .doOnNext(iun -> log.info("iun paper notification failed {}", iun))
                .collectList()
                .flatMapMany(notifications -> checkNotificationFailedList(recipientInternalId, notifications));
    }

    private Mono<FileDownloadResponse> getFileRecursive(Integer n, String fileKey, BigDecimal millis) {
        log.debug("n = {}, fileKey = {}, millis = {}, GetFileRecursive received input", n, fileKey, millis);

        log.debug("n = {}, Are attempts to recover the file ended?", n);
        if (n<0) {
            log.error("errorReason = {}, The file you are trying to recover is not available", ExceptionTypeEnum.DOCUMENT_URL_NOT_FOUND.getMessage());
            return Mono.error(new PnGenericException( ExceptionTypeEnum.DOCUMENT_URL_NOT_FOUND, ExceptionTypeEnum.DOCUMENT_URL_NOT_FOUND.getMessage()));
        } else {
            log.debug("n = {}, There are some other attempts to recover the file", n);
            return Mono.delay(Duration.ofMillis( millis.longValue() ))
                .flatMap(item -> {
                    log.debug("fileKey = {}, Trying to retrieve the file with this filekey", fileKey);
                    return safeStorageClient.getFile(fileKey)
                        .map(fileDownloadResponseDto -> {
                            log.debug("fileKey = {}, fileDownloadResponseDto = {}, The file with this fileKey has been recovered",fileKey, fileDownloadResponseDto);
                            return fileDownloadResponseDto;
                        })
                        .onErrorResume(exception -> {
                            log.error("errorReason = {}, error = {}, Error during retrieving", exception.getMessage(), exception);
                            return Mono.error(exception);
                        })
                        .onErrorResume(PnRetryStorageException.class, exception -> {
                            log.error("errorReason = {}, error = {}, Error during retrieving file", exception.getMessage(), exception);
                            return getFileRecursive(n - 1, fileKey, exception.getRetryAfter());
                        });
            });
        }
    }
    private Mono<Void> traceErrorOnDB(String operationId, Throwable exception) {
        log.debug("operationId = {}, exception = {},TraceErrorOnDB received input", operationId, exception);
        log.error("errorReason = {}, error = {}, Error during the validation flow", exception.getMessage(), exception);
        log.debug("operationId = {}, Retrieving entityOperation from Database", operationId);
        return operationDAO.getByOperationId(operationId)
                .flatMap(operation -> {
                    operation.setErrorReason(exception.getMessage());
                    log.error("errorReason = {}, error = {}, Setting errorReason into entityOperation", exception.getMessage(), exception);
                    return updateOperationStatus(operation, OperationStatusEnum.KO);
                });
    }

    public Flux<List<PnServiceDeskAttachments>> splitAttachments(List<PnServiceDeskAttachments> attachments) {
        return Flux.fromIterable(attachments)
                .groupBy(PnServiceDeskAttachments::getIun)
                .flatMap(group -> group.buffer(100))
                .map(ArrayList::new);
    }

//    public Flux<PnServiceDeskOperations> setAttachmentsListForOperations(PnServiceDeskOperations operations, List<List<PnServiceDeskAttachments>> splitAttachmentsList) {
//        return Flux.fromIterable(splitAttachmentsList)
//                .flatMap(attachments -> {
//                    PnServiceDeskOperations pnServiceDeskOperations = OperationMapper.copyOperation(operations);
//                    pnServiceDeskOperations.setAttachments(attachments);
//
//                    pnServiceDeskOperations.setOperationId(operations.getOperationId().concat(""));
//                    return pnServiceDeskOperations;
//                });
//    }

}