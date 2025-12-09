package it.pagopa.pn.service.desk.service;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import reactor.core.publisher.Mono;

public interface OperationsService {

    Mono<OperationsResponse> createOperation(String xPagopaPnUid, CreateOperationRequest createOperationRequest);
    Mono<OperationsResponse> createActOperation(String xPagopaPnUid, CreateActOperationRequest createActOperationRequest);
    Mono<String> getOperationStatus(String operationId);
    Mono<VideoUploadResponse> presignedUrlVideoUpload(String xPagopaPnUid, String operationId, VideoUploadRequest videoUploadRequest);
    Mono<SearchResponse> searchOperationsFromRecipientInternalId (String xPagopaPnUid, SearchNotificationRequest searchNotificationRequest);
}
