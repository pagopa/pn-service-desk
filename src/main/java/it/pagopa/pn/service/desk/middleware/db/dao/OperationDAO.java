package it.pagopa.pn.service.desk.middleware.db.dao;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.OperationResponse;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OperationDAO {

    Mono<PnServiceDeskOperations> createOperation (PnServiceDeskOperations operations);
    Flux<PnServiceDeskOperations> searchOperationsFromRecipientInternalId (String taxId);
}
