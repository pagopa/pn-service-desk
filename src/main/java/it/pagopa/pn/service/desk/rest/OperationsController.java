package it.pagopa.pn.service.desk.rest;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.api.OperationApi;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.CreateOperationRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.OperationsResponse;
import it.pagopa.pn.service.desk.service.OperationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class OperationsController implements OperationApi {

    @Autowired
    private OperationsService operationsService;

    @Override
    public Mono<ResponseEntity<OperationsResponse>> createOperation(String xPagopaPnUid, Mono<CreateOperationRequest> createOperationRequest, ServerWebExchange exchange) {
        return createOperationRequest
                .flatMap(operationRequest -> operationsService.createOperation(xPagopaPnUid, operationRequest)
                        .map(operationsResponse -> ResponseEntity.status(HttpStatus.CREATED).body(operationsResponse)));
    }
}
