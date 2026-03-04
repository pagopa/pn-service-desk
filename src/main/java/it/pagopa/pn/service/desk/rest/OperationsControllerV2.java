package it.pagopa.pn.service.desk.rest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.api.OperationV2Api;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.GetOperationsResponseV2;
import it.pagopa.pn.service.desk.service.OperationsServiceV2;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@RestController
@AllArgsConstructor
public class OperationsControllerV2 implements OperationV2Api {

    private final OperationsServiceV2 operationServiceV2;

    @Override
    public Mono<ResponseEntity<GetOperationsResponseV2>> getOperationV2(String xPagopaPnUid, String operationId, ServerWebExchange exchange) {
        return Mono.just(operationId)
                   .flatMap(operationServiceV2::getOperation)
                .map(response -> ResponseEntity.status(HttpStatus.OK).body(response));
    }
}
