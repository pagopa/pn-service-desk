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
import it.pagopa.pn.service.desk.mapper.ExternalChannelMapper;
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
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.externalchannel.PnExternalChannelClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.paperchannel.PnPaperChannelClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.safestorage.PnSafeStorageClient;
import it.pagopa.pn.service.desk.model.AttachmentInfo;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import it.pagopa.pn.service.desk.model.SplittingAttachments;
import it.pagopa.pn.service.desk.service.impl.BaseService;
import it.pagopa.pn.service.desk.utility.Utility;
import lombok.CustomLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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
    private PnExternalChannelClient pnExternalChannelClient;
    private ExternalChannelMapper externalChannelMapper;


    public ValidationOperationActionImpl(OperationDAO operationDao, AddressDAO addressDAO,
                                         PnAddressManagerClient addressManagerClient, PnDeliveryPushClient pnDeliveryPushClient,
                                         PnDeliveryClient pnDeliveryClient,
                                         PnPaperChannelClient paperChannelClient, PnSafeStorageClient safeStorageClient,
                                         PnServiceDeskConfigs cfn, PnDataVaultClient pnDataVaultClient, PnExternalChannelClient pnExternalChannelClient, ExternalChannelMapper externalChannelMapper) {
        super(operationDao);
        this.addressDAO = addressDAO;
        this.addressManagerClient = addressManagerClient;
        this.pnDeliveryClient = pnDeliveryClient;
        this.pnDeliveryPushClient = pnDeliveryPushClient;
        this.paperChannelClient = paperChannelClient;
        this.safeStorageClient = safeStorageClient;
        this.cfn = cfn;
        this.pnDataVaultClient = pnDataVaultClient;
        this.pnExternalChannelClient = pnExternalChannelClient;
        this.externalChannelMapper = externalChannelMapper;
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

        if ("EMAIL".equalsIgnoreCase(address.getType())) {
            List<String> iuns = new ArrayList<>();
            iuns.add(operation.getIun());

            return getAttachmentsList(operation, iuns)
                    .collectList()
                    .flatMapMany(lstAttachments -> new SplittingAttachments(lstAttachments, operation, cfn.getMaxNumberOfPages()).splitAttachment())
                    .flatMap(op -> operationDAO.updateEntity(op)
                                               .switchIfEmpty(Mono.defer(() -> Mono.error(new PnGenericException(ERROR_ON_UPDATE_ENTITY, ERROR_ON_UPDATE_ENTITY.getMessage()))))
                                               .thenReturn(op))
                    .flatMap(op -> requestToPrepare(op, address))
                    .then();
        }

        return getIuns(operation.getRecipientInternalId())
                .collectList()
                .flatMap(responsePaperNotificationFailed -> {
                    if (responsePaperNotificationFailed.isEmpty()) {
                        return Mono.error(new PnGenericException(IUNS_ALREADY_IN_PROGRESS, IUNS_ALREADY_IN_PROGRESS.getMessage()));
                    }

                    log.debug("listOfIuns = {}, List of iuns retrivied", responsePaperNotificationFailed);
                    operation.setErrorReason(null);
                    return updateOperationStatus(operation, OperationStatusEnum.VALIDATION).thenReturn(responsePaperNotificationFailed);
                })
                .flatMapMany(iuns -> getAttachmentsList(operation, iuns))
                .collectList()
                .flatMapMany(lstAttachemtns -> new SplittingAttachments(lstAttachemtns, operation, cfn.getMaxNumberOfPages()).splitAttachment())
                .flatMap(op -> {
                    log.info("create entity for {}", op.getOperationId());
                    return operationDAO.updateEntity(op)
                            .switchIfEmpty(Mono.defer(() -> {
                                log.error("entityOperation = {}, Error on update entityOperation", op);
                                return Mono.error(new PnGenericException(ERROR_ON_UPDATE_ENTITY, ERROR_ON_UPDATE_ENTITY.getMessage()));
                            }))
                            .thenReturn(op);
                })
                .flatMap( op-> requestToPrepare(op, address)
)
                .then()
                .doOnError(exception -> log.error("errorReason = {}, Error during the validation flow", exception.getMessage()));
    }

    private Mono<Void> requestToPrepare(PnServiceDeskOperations operation, PnServiceDeskAddress address) {
        return Flux.fromIterable(operation.getAttachments())
                                .flatMap(this::getFileKeyFromAttachments)
                                .map(fileKey -> fileKey)
                                .collectList()
                                .flatMap(attachments -> {
                                    log.info("entityOperation = {}, entityAddress = {}, attachments = {}, All data requirements are available to make the call to prepare", operation, address, attachments);
                                    return "EMAIL".equalsIgnoreCase(address.getType())?
                                            emailPrepare(operation,address,attachments):
                                            paperPrepare(operation, address, attachments);
                                })
                                .onErrorResume(ex -> {
                                    if (ex instanceof PnEntityNotFoundException) {
                                        return Mono.error(ex);
                                    }
                                    return traceErrorOnDB(operation.getOperationId(), ex);
                                });
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

    protected Flux<PnServiceDeskAttachments> getAttachmentsList(PnServiceDeskOperations entityOperation, List<String> iuns) {
        if (iuns != null && !iuns.isEmpty()) {
            return Flux.fromIterable(iuns).flatMap(iun -> getAttachmentsFromIun(entityOperation, iun));
        }
        return Flux.empty();
    }

    private Mono<PnServiceDeskAttachments> getAttachmentsFromIun(PnServiceDeskOperations entityOperation, String iun) {
        log.debug("entityOperation: {}, iun: {}, GetAttachmentsFromIun received input", entityOperation, iun);

        return Mono.just(AttachmentMapper.initAttachment(iun))
                .flatMap(entity ->
                        this.getAttachmentsFromDelivery(iun)
                                .concatWith(
                                        entityOperation.getIun() != null ?
                                                Flux.empty():
                                                getAttachmentsFromDeliveryPush(entityOperation.getRecipientInternalId(), iun))
                                .flatMap(this::attachmentInfo)
                                .filter(attachmentInfo -> !cfn.getDocumentTypeFilter().contains(attachmentInfo.getDocumentType()))
                                .map(attachmentInfo -> {
                                    if (StringUtils.isNotEmpty(attachmentInfo.getFileKey())) attachmentInfo.setFileKey(attachmentInfo.getFileKey().contains(Utility.SAFESTORAGE_BASE_URL) ? attachmentInfo.getFileKey() : Utility.SAFESTORAGE_BASE_URL.concat(attachmentInfo.getFileKey()));
                                    return attachmentInfo;
                                })
                                .collectList()
                                .map(lst -> {
                                    entity.setFilesKey(lst.stream().map(AttachmentInfo::getFileKey).toList());

                                    boolean isAvailable = lst.stream().allMatch(AttachmentInfo::isAvailable);
                                    if (!isAvailable)  {
                                        entity.setIsAvailable(FALSE);
                                        entity.setNumberOfPages(0);
                                        return entity;
                                    } else {
                                        entity.setIsAvailable(TRUE);
                                        entity.setNumberOfPages(lst.stream().reduce(0, (total, element) -> total + element.getNumberOfPage(), Integer::sum));
                                        entity.setNumberOfPages(entity.getNumberOfPages()%2 + entity.getNumberOfPages()/2);
                                        log.info("Number of pages {}", entity.getNumberOfPages());
                                        return entity;
                                    }
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
                                    log.info("fileKey {} numberOfPages: {}", fileKey, info.getNumberOfPage());
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

    private Flux<String> getIuns(String recipientInternalId) {
        log.debug("recipientInternalId = {}, GetIuns received input", recipientInternalId);

        return pnDeliveryPushClient.paperNotificationFailed(recipientInternalId)
                .onErrorResume(ex -> {
                    log.error("recipientInternalId = {}, errorReason = {}, Error on delivery push client", recipientInternalId, ex.getMessage());
                    return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_PUSH_CLIENT, ex.getMessage()));
                })
                .doOnNext(iun -> log.debug("recipientInternalId = {}, iun = {}, Iun retrievied", iun, recipientInternalId))
                .map(ResponsePaperNotificationFailedDtoDto::getIun)
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




    private Mono<Void> paperPrepare(PnServiceDeskOperations entityOperation, PnServiceDeskAddress address, List<String> attachments) {
        log.debug("entityOperation = {}, entityAddress = {}, PaperPrepare received input", entityOperation, address);

        String requestId = checkAndGenerateRequestId(entityOperation, attachments);

        log.debug("recipientInternalId = {}, Calling service for deanonymizing this recipientInternalId", entityOperation.getRecipientInternalId());
        return this.pnDataVaultClient.deAnonymized(entityOperation.getRecipientInternalId())
                .map(fiscalCode -> PaperChannelMapper.getPrepareRequest(entityOperation, address, attachments, requestId, fiscalCode, cfn))
                .flatMap(prepareRequestDto -> {
                    log.info("requestId = {}, prepareRequestDto = {}, Calling prepare api with this requestId and request", requestId, prepareRequestDto);
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

    private Mono<Void> emailPrepare(PnServiceDeskOperations entityOperation, PnServiceDeskAddress address, List<String> attachments) {
        log.debug("entityOperation = {}, entityAddress = {}, EmailPrepare received input", entityOperation, address);

        String requestId = Utility.generateRequestId(entityOperation.getOperationId());

        log.debug("recipientInternalId = {}, Calling service for deanonymizing this recipientInternalId", entityOperation.getRecipientInternalId());
        return this.pnDataVaultClient.deAnonymized(entityOperation.getRecipientInternalId())
                                     .flatMap(fiscalCode -> externalChannelMapper.getPrepareCourtesyMail(entityOperation, address, attachments, requestId))
                                     .flatMap(prepareRequestDto -> {
                                         log.info("requestId = {}, prepareRequestDto = {}, Calling prepare api with this requestId and request", requestId, prepareRequestDto);
                                         return this.pnExternalChannelClient.sendCourtesyMail(requestId, cfn.getExternalChannelCxId(), prepareRequestDto);
                                     })
                                     .doOnSuccess(response -> log.debug("Mail prepare request has been sent"))
                                     .flatMap(response -> {
                                         entityOperation.setErrorReason(null);
                                         log.debug("errorReason = {}, Setting to null errorReason into entityOperation", entityOperation);
                                         return updateOperationStatus(entityOperation, OperationStatusEnum.PROGRESS);
                                     })
                                     .onErrorResume(exception -> {
                                         log.error("requestId = {}, errorReason = {}, Error during call externalChannel prepare", requestId, exception.getMessage());
                                         return Mono.error(new PnGenericException(ERROR_ON_SEND_EXTERNAL_CHANNEL_CLIENT, exception.getMessage()));
                                     })
                                     .then();
    }

    private String checkAndGenerateRequestId(PnServiceDeskOperations entityOperation, List<String> attachments) {
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

        return requestId;
    }


}