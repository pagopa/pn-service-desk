package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.exception.PnRetryStorageException;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.CreateOperationRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.OperationsResponse;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.VideoUploadRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.VideoUploadResponse;
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
import it.pagopa.pn.service.desk.service.OperationsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.*;
import static it.pagopa.pn.service.desk.utility.Utility.CONTENT_TYPE_VALUE;

@Slf4j
@Service
@AllArgsConstructor
public class OperationsServiceImpl implements OperationsService {
    private PnDataVaultClient dataVaultClient;
    private PnSafeStorageClient safeStorageClient;
    private OperationDAO operationDAO;
    private OperationsFileKeyDAO operationsFileKeyDAO;
    private PnServiceDeskConfigs cfn;


    @Override
    public Mono<OperationsResponse> createOperation(String xPagopaPnUid, CreateOperationRequest createOperationRequest) {

        OperationsResponse response = new OperationsResponse();

        return dataVaultClient.anonymized(createOperationRequest.getTaxId())
                .map(recipientId -> OperationMapper.getInitialOperation(createOperationRequest, recipientId))
                .zipWhen(pnServiceDeskOperations -> {
                    PnServiceDeskAddress address = AddressMapper.toEntity(createOperationRequest.getAddress(), pnServiceDeskOperations.getOperationId(), cfn);
                    return Mono.just(address);
                })
                .flatMap(this::checkAndSaveOperation)
                .map(operation -> response.operationId(operation.getOperationId()));
    }

    private Mono<PnServiceDeskOperations> checkAndSaveOperation(Tuple2<PnServiceDeskOperations, PnServiceDeskAddress> operationAndAddress){
        PnServiceDeskOperations operation = operationAndAddress.getT1();
        PnServiceDeskAddress address = operationAndAddress.getT2();
        return operationDAO.getByOperationId(operation.getOperationId())
                .flatMap(response -> Mono.error(new PnGenericException(OPERATION_ID_IS_PRESENT, OPERATION_ID_IS_PRESENT.getMessage(), HttpStatus.BAD_REQUEST)))
                .switchIfEmpty(Mono.defer(() -> operationDAO.createOperationAndAddress(operation, address)))
                .thenReturn(operation);
    }

    @Override
    public Mono<SearchResponse> searchOperationsFromRecipientInternalId(String xPagopaPnUid, SearchNotificationRequest searchNotificationRequest) {
        SearchResponse response = new SearchResponse();

        return dataVaultClient.anonymized(searchNotificationRequest.getTaxId())
                .flatMapMany(taxId -> operationDAO.searchOperationsFromRecipientInternalId(taxId))
                .map(OperationMapper::operationResponseMapper)
                .collectList()
                .map(operations -> {
                    response.setOperations(operations);
                    return response;
                });
    }

    @Override
    public Mono<VideoUploadResponse> presignedUrlVideoUpload(String xPagopaPnUid, String operationId, VideoUploadRequest videoUploadRequest) {
        if (!StringUtils.equalsIgnoreCase(CONTENT_TYPE_VALUE, videoUploadRequest.getContentType())) {
            return Mono.error(new PnGenericException(ERROR_CONTENT_TYPE, ERROR_CONTENT_TYPE.getMessage()));
        }

        return operationDAO.getByOperationId(operationId)
                .switchIfEmpty(Mono.error(new PnGenericException(OPERATION_IS_NOT_PRESENT, OPERATION_IS_NOT_PRESENT.getMessage(), HttpStatus.BAD_REQUEST)))
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
        return operationsFileKeyDAO.getFileKeyByOperationId(operationId)
                .flatMap(operationFileKey -> safeStorageClient.getFile(operationFileKey.getFileKey()))
                .map(response -> operationId)
                .onErrorResume(PnRetryStorageException.class, ex -> Mono.error(new PnGenericException(SAFE_STORAGE_FILE_LOADING, SAFE_STORAGE_FILE_LOADING.getMessage(), HttpStatus.BAD_REQUEST)))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode() == HttpStatus.NOT_FOUND) return Mono.just(operationId);
                    return Mono.error(new PnGenericException(ERROR_DURING_RECOVERING_FILE, ERROR_DURING_RECOVERING_FILE.getMessage(), HttpStatus.BAD_REQUEST));
                });

    }




}
