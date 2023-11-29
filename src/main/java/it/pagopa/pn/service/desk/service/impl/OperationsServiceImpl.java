package it.pagopa.pn.service.desk.service.impl;


import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.exception.PnRetryStorageException;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.service.desk.mapper.AddressMapper;
import it.pagopa.pn.service.desk.mapper.OperationMapper;
import it.pagopa.pn.service.desk.mapper.OperationsFileKeyMapper;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationsFileKeyDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault.PnDataVaultClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.safestorage.PnSafeStorageClient;
import it.pagopa.pn.service.desk.service.AuditLogService;
import it.pagopa.pn.service.desk.service.NotificationService;
import it.pagopa.pn.service.desk.service.OperationsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.OffsetDateTime;
import java.util.UUID;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.*;
import static it.pagopa.pn.service.desk.utility.Utility.CONTENT_TYPE_VALUE;

@Slf4j
@Service
public class OperationsServiceImpl implements OperationsService {
    private final NotificationService notificationService;
    private final PnDataVaultClient dataVaultClient;
    private final PnSafeStorageClient safeStorageClient;
    private final OperationDAO operationDAO;
    private final OperationsFileKeyDAO operationsFileKeyDAO;
    private final PnServiceDeskConfigs cfn;
    private final AuditLogService auditLogService;
    private static final String ERROR_MESSAGE_NO_UNREACHABLE_NOTIFICATIONS = "errorReason = {}, no unreachable notifications found";
    private static final String ERROR_MESSAGE_OPERATION_ALREADY_PRESENT = "errorReason = {}, no unreachable notifications found";
    private static final String ERROR_MESSAGE_INVALID_CONTENT_TYPE = "errorReason = {}, invalid content type";
    private static final String ERROR_MESSAGE_SAFE_STORAGE_FILE_LOADING = "errorReason = {}, file loading";
    private static final String ERROR_MESSAGE_RECOVERING_FILE = "errorReason = {}, error during file recover";

    public OperationsServiceImpl(NotificationService notificationService, PnDataVaultClient dataVaultClient,
                                 PnSafeStorageClient safeStorageClient, OperationDAO operationDAO,
                                 OperationsFileKeyDAO operationsFileKeyDAO, PnServiceDeskConfigs cfn,
                                 AuditLogService auditLogService) {
        this.notificationService = notificationService;
        this.dataVaultClient = dataVaultClient;
        this.safeStorageClient = safeStorageClient;
        this.operationDAO = operationDAO;
        this.operationsFileKeyDAO = operationsFileKeyDAO;
        this.cfn = cfn;
        this.auditLogService = auditLogService;
    }

    @Override
    public Mono<OperationsResponse> createOperation(String xPagopaPnUid, CreateOperationRequest createOperationRequest) {
        log.debug("xPagopaPnUid = {}, createOperationRequest = {}, CreateOperation received input", xPagopaPnUid, createOperationRequest);

        PnAuditLogEvent logEvent = auditLogService.buildAuditLogEvent(PnAuditLogEventType.AUD_NT_INSERT, "creating operation = {}", createOperationRequest.getTaxId());

        OperationsResponse response = new OperationsResponse();
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setTaxId(createOperationRequest.getTaxId());
        String randomUUID = UUID.randomUUID().toString();

        return notificationService.getUnreachableNotification(randomUUID, notificationRequest)
                .flatMap(notificationsUnreachableResponse -> {
                    log.debug("notificationsUnreachableResponse = {}, Are there unreachable notification?", notificationsUnreachableResponse);
                    if(notificationsUnreachableResponse.getNotificationsCount().equals(1L)) {
                        log.debug("notificationsUnreachableCount = {}, There are unreachable notification?", notificationsUnreachableResponse.getNotificationsCount());
                        return dataVaultClient.anonymized(createOperationRequest.getTaxId())
                                .map(recipientId -> OperationMapper.getInitialOperation(createOperationRequest, recipientId))
                                .zipWhen(pnServiceDeskOperations -> {
                                    PnServiceDeskAddress address = AddressMapper.toEntity(createOperationRequest.getAddress(), pnServiceDeskOperations.getOperationId(), cfn);
                                    return Mono.just(address);
                                })
                                .flatMap(this::checkAndSaveOperation)
                                .map(operation -> response.operationId(operation.getOperationId()));
                    }
                    PnGenericException ex = new PnGenericException(NO_UNREACHABLE_NOTIFICATION,NO_UNREACHABLE_NOTIFICATION.getMessage());
                    log.error("notificationsUnreachableCount = {}, There are not unreachable notification", notificationsUnreachableResponse.getNotificationsCount());
                    logEvent.generateFailure(ERROR_MESSAGE_NO_UNREACHABLE_NOTIFICATIONS, ex.getMessage()).log();
                    return Mono.error(ex);
                });
    }

    private Mono<PnServiceDeskOperations> checkAndSaveOperation(Tuple2<PnServiceDeskOperations, PnServiceDeskAddress> operationAndAddress){
        log.debug("entityOperation = {}, entityAddress = {}, CheckAndSaveOperation received input", operationAndAddress.getT1(), operationAndAddress.getT2());

        PnAuditLogEvent logEvent = auditLogService.buildAuditLogEvent(PnAuditLogEventType.AUD_NT_INSERT, "checkAndSaveOperation, operationId = {}", operationAndAddress.getT1().getOperationId());

        PnServiceDeskOperations entityOperation = operationAndAddress.getT1();
        PnServiceDeskAddress entityAddress = operationAndAddress.getT2();

        return operationDAO.getByOperationId(entityOperation.getOperationId())
                .flatMap(entityresponse -> {
                    PnGenericException ex = new PnGenericException(OPERATION_ID_IS_PRESENT, OPERATION_ID_IS_PRESENT.getMessage(), HttpStatus.BAD_REQUEST);
                    log.error("response = {}, The operation id is already present for the ticket id", entityresponse);
                    logEvent.generateFailure(ERROR_MESSAGE_OPERATION_ALREADY_PRESENT, ex.getMessage()).log();
                    return Mono.error(ex);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.debug("entityOperation = {}, entityAddress = {}, Creating operation and address", operationAndAddress.getT1(), operationAndAddress.getT2());
                    logEvent.generateSuccess("creating operation entityOperation = {}, entityAddress = {}", operationAndAddress.getT1(), operationAndAddress.getT2()).log();
                    return operationDAO.createOperationAndAddress(entityOperation, entityAddress);
                }))
                .thenReturn(entityOperation);
    }

    @Override
    public Mono<SearchResponse> searchOperationsFromRecipientInternalId(String xPagopaPnUid, SearchNotificationRequest searchNotificationRequest) {
        SearchResponse response = new SearchResponse();

        PnAuditLogEvent logEvent = auditLogService.buildAuditLogEvent(PnAuditLogEventType.AUD_NT_INSERT, "searchOperationsFromRecipientInternalId for taxId = {}", searchNotificationRequest.getTaxId());

        return dataVaultClient.anonymized(searchNotificationRequest.getTaxId())
                .flatMapMany(taxId -> operationDAO.searchOperationsFromRecipientInternalId(taxId))
                .map(operationResponseMapper -> OperationMapper.operationResponseMapper(cfn, operationResponseMapper, searchNotificationRequest.getTaxId()))
                .collectSortedList((op1, op2) ->
                        (op2.getOperationUpdateTimestamp() != null ? op2.getOperationUpdateTimestamp()  : OffsetDateTime.now())
                                .compareTo((op1.getOperationUpdateTimestamp() != null ? op1.getOperationUpdateTimestamp()  : OffsetDateTime.now())))
                .map(operations -> {
                    response.setOperations(operations);
                    logEvent.generateSuccess("searchOperationsFromRecipientInternalId response = {}", response).log();
                    return response;
                });
    }

    @Override
    public Mono<VideoUploadResponse> presignedUrlVideoUpload(String xPagopaPnUid, String operationId, VideoUploadRequest videoUploadRequest) {
        PnAuditLogEvent logEvent = auditLogService.buildAuditLogEvent(PnAuditLogEventType.AUD_NT_INSERT, "presignedUrlVideoUpload for operationId = {}", operationId);

        if (!StringUtils.equalsIgnoreCase(CONTENT_TYPE_VALUE, videoUploadRequest.getContentType())) {
            PnGenericException ex = new PnGenericException(ERROR_CONTENT_TYPE, ERROR_CONTENT_TYPE.getMessage());
            logEvent.generateFailure(ERROR_MESSAGE_INVALID_CONTENT_TYPE, ex.getMessage()).log();
            return Mono.error(ex);
        }

        return operationDAO.getByOperationId(operationId)
                .switchIfEmpty(Mono.error(new PnGenericException(OPERATION_IS_NOT_PRESENT, OPERATION_IS_NOT_PRESENT.getMessage(), HttpStatus.NOT_FOUND)))
                .flatMap(operation -> manageOperationFileKey(operationId))
                .switchIfEmpty(Mono.just(operationId))
                .flatMap(operationID -> safeStorageClient.getPresignedUrl(videoUploadRequest))
                .flatMap(fileCreationResponse ->
                        operationsFileKeyDAO.updateVideoFileKey(OperationsFileKeyMapper.getOperationFileKey(fileCreationResponse.getKey(), operationId))
                                .thenReturn(fileCreationResponse)
                )
                .map(OperationsFileKeyMapper::getVideoUpload);
    }

    private Mono<String> manageOperationFileKey(String operationId){
        PnAuditLogEvent logEvent = auditLogService.buildAuditLogEvent(PnAuditLogEventType.AUD_NT_INSERT, "manageOperationFileKey for operationId = {}", operationId);
        return operationsFileKeyDAO.getFileKeyByOperationId(operationId)
                .flatMap(operationFileKey -> safeStorageClient.getFile(operationFileKey.getFileKey()))
                .map(response -> {
                    logEvent.generateSuccess("manageOperationFileKey response = {}", response).log();
                    return operationId;
                })
                .onErrorResume(PnRetryStorageException.class, ex -> {
                    logEvent.generateFailure(ERROR_MESSAGE_SAFE_STORAGE_FILE_LOADING, ex.getMessage()).log();
                    return Mono.error(new PnGenericException(SAFE_STORAGE_FILE_LOADING, SAFE_STORAGE_FILE_LOADING.getMessage(), HttpStatus.BAD_REQUEST));
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode() == HttpStatus.NOT_FOUND) return Mono.just(operationId);
                    PnGenericException exception = new PnGenericException(ERROR_DURING_RECOVERING_FILE, ERROR_DURING_RECOVERING_FILE.getMessage(), HttpStatus.BAD_REQUEST);
                    logEvent.generateFailure(ERROR_MESSAGE_RECOVERING_FILE, ex.getMessage()).log();
                    return Mono.error(exception);
                });

    }




}
