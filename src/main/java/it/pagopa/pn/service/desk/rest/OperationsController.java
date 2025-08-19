package it.pagopa.pn.service.desk.rest;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.api.OperationApi;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.service.desk.service.OperationsService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
public class OperationsController implements OperationApi {

    private final OperationsService operationsService;

    @Override
    public Mono<ResponseEntity<OperationsResponse>> createOperation(String xPagopaPnUid, Mono<CreateOperationRequest> createOperationRequest, ServerWebExchange exchange) {
        return createOperationRequest
                .flatMap(operationRequest -> operationsService.createOperation(xPagopaPnUid, operationRequest)
                        .map(operationsResponse -> ResponseEntity.status(HttpStatus.CREATED).body(operationsResponse)));
    }


    @Override
    public Mono<ResponseEntity<OperationsResponse>> createActOperation(String xPagopaPnUid, Mono<CreateActOperationRequest> createActOperationRequest, ServerWebExchange exchange) {
        return createActOperationRequest
                .flatMap(actOperationRequest -> operationsService.createActOperation(xPagopaPnUid, actOperationRequest)
                        .map(actOperationResponse -> ResponseEntity.status(HttpStatus.CREATED).body(actOperationResponse)));
    }

    @Override
    public  Mono<ResponseEntity<SearchResponse>> searchOperationsFromTaxId(String xPagopaPnUid, Mono<SearchNotificationRequest> searchNotificationRequest, final ServerWebExchange exchange) {
        return searchNotificationRequest
                .flatMap(notificationRequest -> operationsService.searchOperationsFromRecipientInternalId(xPagopaPnUid,notificationRequest)
                        .map(notificationResponse -> ResponseEntity.status(HttpStatus.OK).body(notificationResponse)));
    }


    @Override
    public Mono<ResponseEntity<VideoUploadResponse>> presignedUrlVideoUpload(String xPagopaPnUid, String operationId, Mono<VideoUploadRequest> videoUploadRequest, ServerWebExchange exchange) {
        return videoUploadRequest
                .flatMap(videoUpload -> operationsService.presignedUrlVideoUpload(xPagopaPnUid, operationId, videoUpload)
                        .map(videoUploadResponse -> ResponseEntity.status(HttpStatus.OK).body(videoUploadResponse)));
    }

    @Override
    public Mono<ResponseEntity<String>> getOperationStatus(String operationId, ServerWebExchange exchange) {
        return Mono.just(operationId)
                .flatMap(id -> operationsService.getOperationStatus(id))
                        .map(status -> ResponseEntity.status(HttpStatus.OK).body(status));

    }
}
