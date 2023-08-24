package it.pagopa.pn.service.desk.middleware.db.dao;

import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import reactor.core.publisher.Mono;

public interface AddressDAO {

  Mono<PnServiceDeskAddress> createAddress (PnServiceDeskAddress address);

  Mono<PnServiceDeskAddress> getAddress (String operationId);

}
