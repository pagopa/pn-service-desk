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
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskSubOperations;
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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.*;
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

    @Override
    public Mono<CreateOperationsResponseV2> createActOperationV2(String xPagopaPnUid, CreateActOperationRequestV2 request) {
        log.debug("xPagopaPnUid = {}, createActOperationRequestV2 = {}, CreateActOperationV2 received input",
                  xPagopaPnUid, request);

        String taxId = request.getTaxId();
        String parentOperationId = Utility.generateOperationId(request.getTicketId(), request.getTicketOperationId());

        return operationDAO.getByOperationId(parentOperationId)
                .flatMap(existing -> {
                    log.error("parentOperationId={}, Operation id is already present for the ticket id", parentOperationId);
                    return Mono.<CreateOperationsResponseV2>error(new PnGenericException(OPERATION_ID_IS_PRESENT, OPERATION_ID_IS_PRESENT.getMessage(), HttpStatus.BAD_REQUEST));
                })
                .switchIfEmpty(Mono.defer(() -> dataVaultClient.anonymized(taxId)
                        .flatMap(recipientId -> {
                            List<PnServiceDeskSubOperations> validSubOps = new ArrayList<>();
                            List<OperationItemResponse> results = new ArrayList<>();
                            AtomicReference<String> denominationRef = new AtomicReference<>();

                            return Flux.fromIterable(request.getIun())
                            .flatMap(iun ->
                                    pnDeliveryClient.getSentNotificationPrivate(iun)
                                            .map(sentNotification -> {
                                                var matchingRecipient = sentNotification.getRecipients().stream()
                                                        .filter(r -> StringUtils.equalsIgnoreCase(r.getTaxId(), taxId))
                                                        .findFirst();
                                                if (matchingRecipient.isPresent()) {
                                                    denominationRef.compareAndSet(null, matchingRecipient.get().getDenomination());
                                                    PnServiceDeskSubOperations subOp = OperationMapper.getInitialSubOperation(parentOperationId, iun, recipientId, request);
                                                    validSubOps.add(subOp);
                                                    return new OperationItemResponse()
                                                            .iun(iun)
                                                            .status(OperationStatusEnum.CREATING.toString());
                                                } else {
                                                    log.error("recipientId={}, iun={}, Tax ID from request does not match any recipient in the notification",
                                                              recipientId, iun);
                                                    return new OperationItemResponse()
                                                            .iun(iun)
                                                            .status("KO")
                                                            .errorReason("Tax ID does not match any recipient of the notification");
                                                }
                                            })
                                            .onErrorResume(ex -> {
                                                log.error("recipientId={}, iun={}, errorReason={}, Error while calling Delivery service",
                                                          recipientId, iun, ex.getMessage(), ex);
                                                return Mono.just(new OperationItemResponse()
                                                        .iun(iun)
                                                        .status("KO")
                                                        .errorReason(ex.getMessage()));
                                            })
                            )
                            .collectList()
                            .flatMap(itemResults -> {
                                results.addAll(itemResults);
                                if (validSubOps.isEmpty()) {
                                    log.warn("parentOperationId={}, All IUNs failed validation, no DB write performed", parentOperationId);
                                    return Mono.just(new CreateOperationsResponseV2()
                                            .operationId(parentOperationId)
                                            .results(results));
                                }

                                List<String> subOpIds = new ArrayList<>();
                                validSubOps.forEach(sub -> subOpIds.add(sub.getOperationId()));

                                PnServiceDeskOperations parent = OperationMapper.getInitialParentOperation(request, recipientId, parentOperationId, subOpIds);
                                PnServiceDeskAddress address = AddressMapper.toActEntity(request.getAddress(), parentOperationId, cfn, denominationRef.get());

                                return operationDAO.createParentOperationWithSubOpsAndAddress(parent, address, validSubOps)
                                        .map(savedParent -> {
                                            log.debug("parentOperationId={}, V2 parent operation created with {} sub-ops",
                                                      parentOperationId, validSubOps.size());
                                            return new CreateOperationsResponseV2()
                                                    .operationId(savedParent.getOperationId())
                                                    .results(results);
                                        });
                            });
                })));
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
    public Mono<SearchResponse> searchOperationsFromRecipientInternalId(String xPagopaPnUid, SearchNotificationRequest searchNotificationRequest) {
        SearchResponse response = new SearchResponse();

        return dataVaultClient.anonymized(searchNotificationRequest.getTaxId())
                .flatMapMany(operationDAO::searchOperationsFromRecipientInternalId)
                .map(operationResponseMapper -> OperationMapper.operationResponseMapper(cfn, operationResponseMapper, searchNotificationRequest.getTaxId()))
                .collectSortedList((op1, op2) ->
                        (op2.getOperationUpdateTimestamp() != null ? op2.getOperationUpdateTimestamp()  : OffsetDateTime.now())
                                .compareTo((op1.getOperationUpdateTimestamp() != null ? op1.getOperationUpdateTimestamp()  : OffsetDateTime.now())))
                .map(operations -> {
                    response.setOperations(operations);
                    return response;
                });
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
                           .map(operation -> operation.getStatus())
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
}
