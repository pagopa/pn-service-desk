package it.pagopa.pn.service.desk.middleware.db.dao;

import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskSubOperations;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;

public interface SubOperationDAO {

    Mono<PnServiceDeskSubOperations> getByOperationId(String operationId);

    Mono<PnServiceDeskSubOperations> updateEntity(PnServiceDeskSubOperations subOperation);

    void createTransaction(TransactWriteItemsEnhancedRequest.Builder builder,
                           PnServiceDeskSubOperations subOperation);
}
