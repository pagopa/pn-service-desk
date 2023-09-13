package it.pagopa.pn.service.desk.middleware.db.dao;


import it.pagopa.pn.service.desk.middleware.entities.PnClientID;
import reactor.core.publisher.Mono;

public interface PnClientDAO {

    Mono<PnClientID> getByApiKey(String apiKey);

    Mono<PnClientID> getByPrefix(String prefixValue);

}
