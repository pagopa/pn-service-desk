package it.pagopa.pn.service.desk.middleware.db.dao;

import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskSubOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;

public interface OperationDAO {

    Mono<Tuple2<PnServiceDeskOperations, PnServiceDeskAddress>> createOperationAndAddress(PnServiceDeskOperations operations, PnServiceDeskAddress pnServiceDeskAddress);
    Mono<PnServiceDeskOperations> createOperation(PnServiceDeskOperations operations);
    Mono<PnServiceDeskOperations> createParentOperationWithSubOpsAndAddress(PnServiceDeskOperations parent, PnServiceDeskAddress address, List<PnServiceDeskSubOperations> subOperations);
    Flux<PnServiceDeskOperations> searchOperationsFromRecipientInternalId(String taxId);
    Mono<PnServiceDeskOperations> getByOperationId(String operationId);
    Mono<PnServiceDeskOperations> updateEntity(PnServiceDeskOperations operations);
}
