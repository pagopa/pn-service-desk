package it.pagopa.pn.service.desk.service;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.ResponseApiKeys;
import reactor.core.publisher.Mono;

public interface ApiKeysService {

    Mono<ResponseApiKeys> getApiKeys(String paId);

}