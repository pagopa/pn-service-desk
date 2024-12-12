package it.pagopa.pn.service.desk.rest;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.api.PaApi;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.PaNotificationsRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.PaSummary;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.SearchNotificationsResponse;
import it.pagopa.pn.service.desk.service.InfoPaService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
public class InfoPaController implements PaApi {

    private final InfoPaService infoPaService;

    @Override
    public Mono<ResponseEntity<Flux<PaSummary>>> getListOfOnboardedPA(String xPagopaPnUid, String paNameFilter, final ServerWebExchange exchange) {
        return this.infoPaService.getListOfOnboardedPA(xPagopaPnUid, paNameFilter).collectList().map(list -> ResponseEntity.ok(Flux.fromStream(list.stream())));
    }

    @Override
    public Mono<ResponseEntity<SearchNotificationsResponse>> searchNotificationsFromSenderId(String xPagopaPnUid, Integer size, String nextPagesKey, Mono<PaNotificationsRequest> paNotificationsRequest, final ServerWebExchange exchange){
        return paNotificationsRequest
                .flatMap(notificationsRequest -> this.infoPaService.searchNotificationsFromSenderId(xPagopaPnUid, size, nextPagesKey, notificationsRequest)
                        .map(searchNotificationsResponse -> ResponseEntity.status(HttpStatus.OK).body(searchNotificationsResponse)));
    }

}
