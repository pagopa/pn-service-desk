package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.CreateOperationRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.OperationsResponse;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.VideoUploadRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.VideoUploadResponse;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.service.desk.mapper.AddressMapper;
import it.pagopa.pn.service.desk.mapper.OperationMapper;
import it.pagopa.pn.service.desk.mapper.OperationsFileKeyMapper;
import it.pagopa.pn.service.desk.middleware.db.dao.AddressDAO;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationsFileKeyDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault.PnDataVaultClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.safestorage.PnSafeStorageClient;
import it.pagopa.pn.service.desk.service.OperationsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.OPERATION_ID_IS_PRESENT;

@Slf4j
@Service
@AllArgsConstructor
public class OperationsServiceImpl implements OperationsService {
    private PnDataVaultClient dataVaultClient;
    private PnSafeStorageClient safeStorageClient;
    private OperationDAO operationDAO;
    private AddressDAO addressDAO;
    private OperationsFileKeyDAO operationsFileKeyDAO;
    private PnServiceDeskConfigs cfn;


    @Override
    public Mono<OperationsResponse> createOperation(String xPagopaPnUid, CreateOperationRequest createOperationRequest) {

        OperationsResponse response = new OperationsResponse();

        return dataVaultClient.anonymized(createOperationRequest.getTaxId())
                .map(recipientId -> OperationMapper.getInitialOperation(createOperationRequest, recipientId))
                .flatMap(this::checkAndSaveOperation)
                .map(pnServiceDeskOperations ->
                        AddressMapper.toEntity(createOperationRequest.getAddress(), pnServiceDeskOperations.getOperationId(), cfn)
                )
                .flatMap(address -> addressDAO.createAddress(address))
                .map(address -> response.operationId(address.getOperationId()));
    }

    private Mono<PnServiceDeskOperations> checkAndSaveOperation(PnServiceDeskOperations operation){
        return operationDAO.getByOperationId(operation.getOperationId())
                .flatMap(response -> Mono.error(new PnGenericException(OPERATION_ID_IS_PRESENT, OPERATION_ID_IS_PRESENT.getMessage(), HttpStatus.BAD_REQUEST)))
                .switchIfEmpty(Mono.defer(() -> operationDAO.createOperation(operation)))
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

        return operationDAO.getByOperationId(operationId)
                .flatMap(operation -> safeStorageClient.getPresignedUrl(videoUploadRequest))
                .doOnNext(fileCreationResponse ->
                    operationsFileKeyDAO.updateVideoFileKey(OperationsFileKeyMapper.getOperationFileKey(fileCreationResponse.getKey(), operationId))
                )
                .map(OperationsFileKeyMapper::getVideoUpload);
    }




}
