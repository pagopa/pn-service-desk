package it.pagopa.pn.service.desk.middleware.db.dao;


import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;

public interface AddressDAO {


  Mono<PnServiceDeskAddress> getAddress (String operationId);

  void createWithTransaction(TransactWriteItemsEnhancedRequest.Builder builder, PnServiceDeskAddress pnAddress);

}
