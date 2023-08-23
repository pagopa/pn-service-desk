package it.pagopa.pn.service.desk.middleware.db.dao;

import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import reactor.core.publisher.Mono;

public interface OperationDAO {

    Mono<PnServiceDeskOperations> createOperation (PnServiceDeskOperations operations);

    Mono<PnServiceDeskOperations> getByOperationId (String operationId);
}
