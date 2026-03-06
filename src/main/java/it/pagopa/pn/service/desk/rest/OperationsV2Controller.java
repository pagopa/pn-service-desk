package it.pagopa.pn.service.desk.rest;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.api.OperationV2Api;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.CreateActOperationRequestV2;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.CreateOperationsResponseV2;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.GetOperationsResponseV2;
import it.pagopa.pn.service.desk.service.OperationsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class OperationsV2Controller implements OperationV2Api {

    private final OperationsService operationsService;

    public OperationsV2Controller(OperationsService operationsService) {
        this.operationsService = operationsService;
    }

    @Override
    public Mono<ResponseEntity<CreateOperationsResponseV2>> createActOperationV2(String xPagopaPnUid, Mono<CreateActOperationRequestV2> requestMono, ServerWebExchange exchange) {
        return requestMono.flatMap(request -> operationsService.createActOperationV2(xPagopaPnUid, request)
                .map(operationsResponse -> ResponseEntity.status(HttpStatus.OK).body(operationsResponse)));
    }

    @Override
    public Mono<ResponseEntity<GetOperationsResponseV2>> getOperationV2(String operationId, ServerWebExchange exchange) {
        return Mono.just(operationId)
                .flatMap(operationsService::getOperationV2)
                .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
    }
}
