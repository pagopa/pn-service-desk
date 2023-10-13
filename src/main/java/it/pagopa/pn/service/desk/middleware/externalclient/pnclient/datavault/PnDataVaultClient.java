package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault;

import reactor.core.publisher.Mono;

public interface PnDataVaultClient {

    Mono<String> anonymized(String data);
    Mono<String> anonymized(String data, String recipientType);
    Mono<String> deAnonymized(String recipientInternalId);
}
