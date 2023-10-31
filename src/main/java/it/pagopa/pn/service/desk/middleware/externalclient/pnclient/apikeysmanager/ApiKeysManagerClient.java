package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.apikeysmanager;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pnapikeymanager.v1.dto.ResponseApiKeysDto;
import reactor.core.publisher.Mono;

public interface ApiKeysManagerClient {

    Mono<ResponseApiKeysDto> getBoApiKeys(String paId);

}
