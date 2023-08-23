package it.pagopa.pn.service.desk.service;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.CreateOperationRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.OperationsResponse;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.SearchNotificationRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.SearchResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.VideoUploadRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.VideoUploadResponse;
import reactor.core.publisher.Mono;

public interface OperationsService {

    Mono<OperationsResponse> createOperation(String xPagopaPnUid, CreateOperationRequest createOperationRequest);
    Mono<VideoUploadResponse> presignedUrlVideoUpload(String xPagopaPnUid, String operationId, VideoUploadRequest videoUploadRequest);
    Mono<SearchResponse> searchOperationsFromRecipientInternalId (String xPagopaPnUid, SearchNotificationRequest searchNotificationRequest);
}
