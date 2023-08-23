package it.pagopa.pn.service.desk.middleware.db.dao;

import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperationFileKey;
import reactor.core.publisher.Mono;

public interface OperationsFileKeyDAO {

    Mono<PnServiceDeskOperationFileKey> updateVideoFileKey(PnServiceDeskOperationFileKey operationFileKey);

}
