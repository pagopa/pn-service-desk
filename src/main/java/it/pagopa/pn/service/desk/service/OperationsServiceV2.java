package it.pagopa.pn.service.desk.service;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.GetOperationsResponseV2;
import reactor.core.publisher.Mono;

public interface OperationsServiceV2 {



    Mono<GetOperationsResponseV2> getOperation(String operationId);

}
