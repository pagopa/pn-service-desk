package it.pagopa.pn.service.desk.service;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.PaNotificationsRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.PaSummary;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.SearchNotificationsResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InfoPaService {

    Flux<PaSummary> getListOfOnboardedPA(String xPagopaPnUid, String paNameFilter);
    Mono<SearchNotificationsResponse> searchNotificationsFromSenderId(String xPagopaPnUid, Integer size, String nextPagesKey, PaNotificationsRequest paNotificationsRequest);

}
