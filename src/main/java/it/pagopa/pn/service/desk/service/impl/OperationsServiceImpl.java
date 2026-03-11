package it.pagopa.pn.service.desk.service.impl;


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
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault.PnDataVaultClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.safestorage.PnSafeStorageClient;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import it.pagopa.pn.service.desk.service.NotificationService;
import it.pagopa.pn.service.desk.service.OperationsService;
import it.pagopa.pn.service.desk.utility.Utility;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.*;
import static it.pagopa.pn.service.desk.model.OperationStatusEnum.CREATING;
import static it.pagopa.pn.service.desk.model.OperationStatusEnum.KO;
import static it.pagopa.pn.service.desk.utility.Utility.CONTENT_TYPE_VALUE;

@Slf4j
@Service
public class OperationsServiceImpl implements OperationsService {
    private final NotificationService notificationService;
    private final PnDataVaultClient dataVaultClient;
    private final PnSafeStorageClient safeStorageClient;
    private final PnDeliveryClient pnDeliveryClient;
    private final OperationDAO operationDAO;
    private final OperationsFileKeyDAO operationsFileKeyDAO;
    private final PnServiceDeskConfigs cfn;
    private record IunResult(OperationItemResponse response, PnServiceDeskOperations subOp, String denomination) {}

    public OperationsServiceImpl(NotificationService notificationService, PnDataVaultClient dataVaultClient,
                                 PnSafeStorageClient safeStorageClient, PnDeliveryClient pnDeliveryClient, OperationDAO operationDAO,
                                 OperationsFileKeyDAO operationsFileKeyDAO, PnServiceDeskConfigs cfn) {
        this.notificationService = notificationService;
        this.dataVaultClient = dataVaultClient;
        this.safeStorageClient = safeStorageClient;
        this.pnDeliveryClient = pnDeliveryClient;
        this.operationDAO = operationDAO;
        this.operationsFileKeyDAO = operationsFileKeyDAO;
        this.cfn = cfn;
    }


    @Override
    public Mono<OperationsResponse> createOperation(String xPagopaPnUid, CreateOperationRequest createOperationRequest) {
        log.debug("xPagopaPnUid = {}, createOperationRequest = {}, CreateOperation received input", xPagopaPnUid, createOperationRequest);

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
                    return Mono.error(ex);
                });
    }

    @Override
    public Mono<OperationsResponse> createActOperation(String xPagopaPnUid, CreateActOperationRequest createActOperationRequest) {

        log.debug("xPagopaPnUid = {}, createActOperationRequest = {}, CreateActOperation received input",
                  xPagopaPnUid, createActOperationRequest);

        String taxId = createActOperationRequest.getTaxId();
        String iun = createActOperationRequest.getIun();

        return dataVaultClient.anonymized(taxId)
                              .flatMap(recipientId ->
                                               pnDeliveryClient.getSentNotificationPrivate(iun)
                                                               .flatMap(sentNotification -> {
                                                                   log.debug("sentNotificationResponse = {}, recipientId={}, retrieving notification to check recipient",
                                                                             sentNotification, recipientId);
                                                                   return sentNotification.getRecipients()
                                                                                          .stream()
                                                                                          .filter(recipient -> StringUtils.equalsIgnoreCase(recipient.getTaxId(), taxId))
                                                                                          .findFirst()
                                                                                          .map(recipient ->
                                                                                                       Mono.just(OperationMapper.getInitialActOperation(createActOperationRequest, recipientId))
                                                                                                           .zipWhen(pnServiceDeskOperations -> {
                                                                                                               PnServiceDeskAddress address = AddressMapper.toActEntity(
                                                                                                                       createActOperationRequest.getAddress(),
                                                                                                                       pnServiceDeskOperations.getOperationId(),
                                                                                                                       cfn,
                                                                                                                       recipient.getDenomination());
                                                                                                               return Mono.just(address);
                                                                                                           })
                                                                                                           .flatMap(this::checkAndSaveOperation)
                                                                                                           .map(operation -> {
                                                                                                               log.debug("ActOperation created successfully for recipientId={}, iun={}", recipientId, iun);
                                                                                                               return new OperationsResponse().operationId(operation.getOperationId());
                                                                                                           })
                                                                                              )
                                                                                          .orElseThrow(() -> {
                                                                                              String errorMsg = "Tax ID from request does not match the Tax ID from the notification";
                                                                                              log.error("recipientId={}, iun={}, {}", recipientId, iun, errorMsg);
                                                                                              return new PnGenericException(NOT_NOTIFICATION_FOUND, errorMsg);
                                                                                          });
                                                               })
                                                               .onErrorResume(exception -> {
                                                                   log.error("recipientId={}, iun={}, errorReason={}, Error while calling Delivery notifications service",
                                                                             recipientId, iun, exception.getMessage(), exception);
                                                                   return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_CLIENT, exception.getMessage()));
                                                               })
                                      );
    }

    private Mono<PnServiceDeskOperations> checkAndSaveOperation(Tuple2<PnServiceDeskOperations, PnServiceDeskAddress> operationAndAddress){
        log.debug("entityOperation = {}, entityAddress = {}, CheckAndSaveOperation received input", operationAndAddress.getT1(), operationAndAddress.getT2());

        PnServiceDeskOperations entityOperation = operationAndAddress.getT1();
        PnServiceDeskAddress entityAddress = operationAndAddress.getT2();

        return operationDAO.getByOperationId(entityOperation.getOperationId())
                .flatMap(entityresponse -> {
                    PnGenericException ex = new PnGenericException(OPERATION_ID_IS_PRESENT, OPERATION_ID_IS_PRESENT.getMessage(), HttpStatus.BAD_REQUEST);
                    log.error("response = {}, The operation id is already present for the ticket id", entityresponse);
                    return Mono.error(ex);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.debug("entityOperation = {}, entityAddress = {}, Creating operation and address", operationAndAddress.getT1(), operationAndAddress.getT2());
                    return operationDAO.createOperationAndAddress(entityOperation, entityAddress);
                }))
                .thenReturn(entityOperation);
    }

    @Override
    public Mono<CreateOperationsResponseV2> createActOperationV2(String xPagopaPnUid, CreateActOperationRequestV2 request) {
        log.debug("xPagopaPnUid = {}, createActOperationRequestV2 = {}, CreateActOperationV2 received input", xPagopaPnUid, request);
        String taxId = request.getTaxId();
        String parentOperationId = Utility.generateOperationId(request.getTicketId(), request.getTicketOperationId());
        return validateNoDuplicateIuns(request.getIun())
                .then(ensureOperationNotExists(parentOperationId))
                .then(dataVaultClient.anonymized(taxId))
                .flatMap(recipientId -> processAllIuns(request, taxId, recipientId, parentOperationId));
    }

    private Mono<Void> validateNoDuplicateIuns(List<String> iuns) {
        if (iuns != null && iuns.size() != new HashSet<>(iuns).size()) {
            return Mono.error(new PnGenericException(DUPLICATE_IUN_IN_REQUEST, DUPLICATE_IUN_IN_REQUEST.getMessage(), HttpStatus.BAD_REQUEST));
        }
        return Mono.empty();
    }

    private Mono<Void> ensureOperationNotExists(String parentOperationId) {
        return operationDAO.getByOperationId(parentOperationId)
                .flatMap(existing -> {
                    PnGenericException ex = new PnGenericException(OPERATION_ID_IS_PRESENT, OPERATION_ID_IS_PRESENT.getMessage(), HttpStatus.BAD_REQUEST);
                    log.error("response = {}, The operation id is already present for the ticket id", existing);
                    return Mono.error(ex);
                })
                .then();
    }

    private Mono<CreateOperationsResponseV2> processAllIuns(CreateActOperationRequestV2 request, String taxId, String recipientId, String parentOperationId) {
        return Flux.fromIterable(request.getIun())
                .flatMap(iun -> processIun(iun, taxId, recipientId, parentOperationId, request))
                .collectList()
                .flatMap(iunResults -> persistParentOperation(request, recipientId, parentOperationId, iunResults));
    }

    private Mono<IunResult> processIun(String iun, String taxId, String recipientId, String parentOperationId, CreateActOperationRequestV2 request) {
        return pnDeliveryClient.getSentNotificationPrivate(iun)
                .map(sentNotification -> sentNotification.getRecipients().stream()
                        .filter(r -> StringUtils.equalsIgnoreCase(r.getTaxId(), taxId))
                        .findFirst()
                        .map(recipient -> {
                            PnServiceDeskOperations subOp = OperationMapper.getInitialSubOperation(parentOperationId, iun, recipientId, request);
                            return new IunResult(new OperationItemResponse().iun(iun).status(CREATING.toString()), subOp, recipient.getDenomination());
                        })
                        .orElseThrow(() -> new PnGenericException(NOT_NOTIFICATION_FOUND, "Tax ID from request does not match the Tax ID from the notification")))
                .onErrorResume(ex -> {
                    log.error("recipientId={}, iun={}, errorReason={}, Error while creating subOperation", recipientId, iun, ex.getMessage(), ex);
                    PnServiceDeskOperations subOp = OperationMapper.getFailedSubOperation(parentOperationId, iun, recipientId, ex.getMessage(), request);
                    return Mono.just(new IunResult(new OperationItemResponse().iun(iun).status(KO.toString()).errorReason(ex.getMessage()), subOp, null));
                });
    }

    private Mono<CreateOperationsResponseV2> persistParentOperation(CreateActOperationRequestV2 request, String recipientId, String parentOperationId, List<IunResult> iunResults) {
        List<OperationItemResponse> responses = iunResults.stream().map(r -> r.response).toList();
        List<PnServiceDeskOperations> subOps = iunResults.stream().filter(r -> r.subOp != null).map(r -> r.subOp).toList();
        List<String> subOpIds = subOps.stream().map(PnServiceDeskOperations::getOperationId).toList();
        boolean allSubOpsFailed = subOps.stream().allMatch(subOp -> KO.toString().equals(subOp.getStatus()));

        if (allSubOpsFailed) {
            log.warn("parentOperationId={}, All IUNs failed validation, creating parent operation with KO status", parentOperationId);
            PnServiceDeskOperations koParent = OperationMapper.getInitialParentOperation(request, recipientId, parentOperationId, subOpIds);
            koParent.setStatus(KO.toString());
            return operationDAO.createParentOperationWithSubOps(koParent, subOps)
                    .map(saved -> new CreateOperationsResponseV2().operationId(saved.getOperationId()).results(responses));
        }

        String denomination = iunResults.stream().map(r -> r.denomination).filter(Objects::nonNull).findFirst().orElse(null);
        PnServiceDeskOperations parent = OperationMapper.getInitialParentOperation(request, recipientId, parentOperationId, subOpIds);
        PnServiceDeskAddress address = AddressMapper.toActEntity(request.getAddress(), parentOperationId, cfn, denomination);

        return operationDAO.createParentOperationWithSubOpsAndAddress(parent, address, subOps)
                .map(saved -> {
                    log.debug("parentOperationId={}, V2 parent operation created with {} sub-ops", parentOperationId, subOps.size());
                    return new CreateOperationsResponseV2().operationId(saved.getOperationId()).results(responses);
                });
    }

    @Override
    public Mono<SearchResponse> searchOperationsFromRecipientInternalId(String xPagopaPnUid, SearchNotificationRequest searchNotificationRequest) {
        SearchResponse response = new SearchResponse();

        return dataVaultClient.anonymized(searchNotificationRequest.getTaxId())
                .flatMapMany(operationDAO::searchOperationsFromRecipientInternalId)
                .filter(operation -> !Boolean.TRUE.equals(operation.getIsSubOperation()))
                .map(pnServiceDeskOperations -> Tuples.of(
                        Objects.requireNonNullElse(pnServiceDeskOperations.getAttachments(), new ArrayList<PnServiceDeskAttachments>()),
                        OperationMapper.operationResponseMapper(pnServiceDeskOperations, searchNotificationRequest.getTaxId())
                ))
                .flatMap(tuple -> enhanceIuns(tuple.getT1(), tuple.getT2()))
                .collectSortedList((op1, op2) ->
                        (op2.getOperationUpdateTimestamp() != null ? op2.getOperationUpdateTimestamp()  : OffsetDateTime.now())
                                .compareTo((op1.getOperationUpdateTimestamp() != null ? op1.getOperationUpdateTimestamp()  : OffsetDateTime.now())))
                .map(operations -> {
                    response.setOperations(operations);
                    return response;
                });
    }

    private Mono<OperationResponse> enhanceIuns(List<PnServiceDeskAttachments> attachments, OperationResponse operationResponse) {
        return Flux.fromIterable(attachments)
                   .flatMap(att -> pnDeliveryClient.getSentNotificationPrivate(att.getIun()))
                   .doOnNext(att -> {
                       SDNotificationSummary summary = new SDNotificationSummary();
                       summary.setIun(att.getIun());
                       summary.setSenderPaInternalId(att.getSenderPaId());
                       summary.setSenderPaIpaCode("");
                       summary.setSenderPaTaxCode(att.getSenderTaxId());
                       summary.setSenderPaDescription(att.getSenderDenomination());
                       if (Boolean.TRUE.equals(att.getDocumentsAvailable())) {
                           operationResponse.getIuns().add(summary);
                       } else {
                           operationResponse.getUncompletedIuns().add(summary);
                       }
                   })
                   .then(Mono.just(operationResponse));
    }

    @Override
    public Mono<VideoUploadResponse> presignedUrlVideoUpload(String xPagopaPnUid, String operationId, VideoUploadRequest videoUploadRequest) {

        if (!StringUtils.equalsIgnoreCase(CONTENT_TYPE_VALUE, videoUploadRequest.getContentType())) {
            PnGenericException ex = new PnGenericException(ERROR_CONTENT_TYPE, ERROR_CONTENT_TYPE.getMessage());
            return Mono.error(ex);
        }

        return operationDAO.getByOperationId(operationId)
                .switchIfEmpty(Mono.error(new PnGenericException(OPERATION_IS_NOT_PRESENT, OPERATION_IS_NOT_PRESENT.getMessage(), HttpStatus.NOT_FOUND)))
                .filter(operation -> operation.getStatus().equals(NotificationStatus.StatusEnum.CREATING.getValue()))
                .switchIfEmpty(Mono.error(new PnGenericException(FILE_ALREADY_UPLOADED, FILE_ALREADY_UPLOADED.getMessage(), HttpStatus.CONFLICT)))
                .flatMap(operation -> manageOperationFileKey(operationId)
                        .switchIfEmpty(Mono.just(operationId))
                        .flatMap(operationID -> safeStorageClient.getPresignedUrl(videoUploadRequest))
                        .flatMap(fileCreationResponse ->
                                operationsFileKeyDAO.updateVideoFileKey(OperationsFileKeyMapper.getOperationFileKey(fileCreationResponse.getKey(), operationId))
                                        .thenReturn(fileCreationResponse)
                        )
                        .flatMap(fileCreationResponse -> {
                            operation.setStatus(OperationStatusEnum.VALIDATION.toString());
                            return operationDAO.updateEntity(operation)
                                    .thenReturn(fileCreationResponse);
                        })
                )
                .map(OperationsFileKeyMapper::getVideoUpload);
    }

    private Mono<String> manageOperationFileKey(String operationId){
        return operationsFileKeyDAO.getFileKeyByOperationId(operationId)
                .flatMap(operationFileKey -> safeStorageClient.getFile(operationFileKey.getFileKey()))
                .map(response -> operationId)
                .onErrorResume(PnRetryStorageException.class, ex -> Mono.error(new PnGenericException(SAFE_STORAGE_FILE_LOADING, SAFE_STORAGE_FILE_LOADING.getMessage(), HttpStatus.BAD_REQUEST)))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode() == HttpStatus.NOT_FOUND) return Mono.just(operationId);
                    PnGenericException exception = new PnGenericException(ERROR_DURING_RECOVERING_FILE, ERROR_DURING_RECOVERING_FILE.getMessage(), HttpStatus.BAD_REQUEST);
                    return Mono.error(exception);
                });

    }


    @Override
    public Mono<String> getOperationStatus(String operationId) {
        return operationDAO.getByOperationId(operationId)
                           .switchIfEmpty(Mono.error(new PnGenericException(OPERATION_IS_NOT_PRESENT,
                                                                            OPERATION_IS_NOT_PRESENT.getMessage(),
                                                                            HttpStatus.NOT_FOUND)))
                           .map(PnServiceDeskOperations::getStatus)
                           .onErrorResume(PnRetryStorageException.class,
                                          ex -> Mono.error(new PnGenericException(SAFE_STORAGE_FILE_LOADING,
                                                                                  SAFE_STORAGE_FILE_LOADING.getMessage(),
                                                                                  HttpStatus.BAD_REQUEST)))
                           .onErrorResume(WebClientResponseException.class, ex -> {
                               if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                                   return Mono.error(new PnGenericException(OPERATION_IS_NOT_PRESENT,
                                                                            OPERATION_IS_NOT_PRESENT.getMessage(),
                                                                            HttpStatus.NOT_FOUND));
                               }
                               return Mono.error(new PnGenericException(ERROR_DURING_RECOVERING_FILE,
                                                                        ERROR_DURING_RECOVERING_FILE.getMessage(),
                                                                        HttpStatus.BAD_REQUEST));
                           });
    }

    @Override
    public Mono<GetOperationsResponseV2> getOperationV2(String operationId) {
        GetOperationsResponseV2 response = new GetOperationsResponseV2();
        log.info("Getting operation with id {}", operationId);
        return operationDAO.getByOperationId(operationId)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Operation with id {} not found", operationId);
                    return Mono.error(new PnGenericException(OPERATION_IS_NOT_PRESENT, OPERATION_IS_NOT_PRESENT.getMessage(), HttpStatus.NOT_FOUND));
                }))
                .doOnNext(op -> log.info("Operation with id {} found, status: {}", operationId, op.getStatus()))
                .filter(operation -> !Boolean.TRUE.equals(operation.getIsSubOperation()))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Operation is a sub-operation - operationId={}", operationId);
                    return Mono.error(new PnGenericException(OPERATION_IS_NOT_PRESENT, OPERATION_IS_NOT_PRESENT.getMessage(), HttpStatus.NOT_FOUND));
                }))
                .flatMap(operation -> {
                    response.setStatus(operation.getStatus());
                    response.setErrorReason(operation.getErrorReason());
                    List<String> subOpIds = operation.getSubOperationsIds();
                    return StringUtils.isNotBlank(operation.getIun())
                            ? buildOperationResponseV1(operation, response).doOnSuccess( res -> log.info("getOperation completed for V1 operation - operationId={}, finalStatus={}", operationId, res.getStatus()))
                            : Flux.fromIterable(subOpIds != null ? subOpIds : List.of())
                            .flatMap(operationDAO::getByOperationId)
                            .doOnNext(subOp -> log.info("SubOperation - id={}, status={}", subOp.getOperationId(), subOp.getStatus()))
                            .map(this::toOperationDetail)
                            .doOnNext(response::addSubOperationsItem)
                            .then(Mono.just(response))
                            .doOnSuccess(res -> {
                                    int subOperationsCount = res.getSubOperations() != null ? res.getSubOperations().size() : 0;
                                    log.info("getOperation completed - operationId={}, finalStatus={}, subOperationsCount={}", operationId, res.getStatus(), subOperationsCount);
                                    });
                })
                .onErrorResume(WebClientResponseException.class, exception -> {
                    log.error( "Error during get operation with id {}, error: {}", operationId, exception.getMessage());
                    return Mono.error(new PnGenericException(ERROR_DURING_GET_OPERATION_V2, ERROR_DURING_GET_OPERATION_V2.getMessage(), HttpStatus.BAD_REQUEST));
                });
    }

    private static Mono<GetOperationsResponseV2> buildOperationResponseV1(PnServiceDeskOperations operation, GetOperationsResponseV2 response) {
        log.info("Operation with id {} is a V1-operation, building response with iun {}", operation.getOperationId(), operation.getIun());
        response.setIun(operation.getIun());

        return Mono.just(response);
    }

    private OperationDetail toOperationDetail(PnServiceDeskOperations subOperation) {
        OperationDetail detail = new OperationDetail();
        detail.setStatus(subOperation.getStatus());
        detail.setIun(subOperation.getIun());
        detail.setErrorReason(subOperation.getErrorReason());
        return detail;
    }
}
