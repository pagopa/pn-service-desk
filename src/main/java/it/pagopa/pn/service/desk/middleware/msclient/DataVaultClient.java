package it.pagopa.pn.service.desk.middleware.msclient;

import reactor.core.publisher.Mono;

public interface DataVaultClient {

    Mono<String> anonymized(String data);
}
