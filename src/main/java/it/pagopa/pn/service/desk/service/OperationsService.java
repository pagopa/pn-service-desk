package it.pagopa.pn.service.desk.service;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OperationsService {

    Mono<OperationsResponse> createOperation(String xPagopaPnUid, CreateOperationRequest createOperationRequest);
    Mono<OperationsResponse> createActOperation(String xPagopaPnUid, CreateActOperationRequest createActOperationRequest);
    Mono<VideoUploadResponse> presignedUrlVideoUpload(String xPagopaPnUid, String operationId, VideoUploadRequest videoUploadRequest);
    Mono<SearchResponse> searchOperationsFromRecipientInternalId (String xPagopaPnUid, SearchNotificationRequest searchNotificationRequest);
}
