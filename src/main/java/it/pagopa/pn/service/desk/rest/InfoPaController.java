package it.pagopa.pn.service.desk.rest;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.api.PaApi;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.PaNotificationsRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.PaSummary;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.SearchNotificationsResponse;
import it.pagopa.pn.service.desk.service.InfoPaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class InfoPaController implements PaApi {

    @Autowired
    private InfoPaService infoPaService; //FIXME constructor injection

    @Override
    public Mono<ResponseEntity<Flux<PaSummary>>> getListOfOnboardedPA(String xPagopaPnUid, final ServerWebExchange exchange) {
        return this.infoPaService.getListOfOnboardedPA(xPagopaPnUid).collectList().map(list -> ResponseEntity.ok(Flux.fromStream(list.stream())));
    }

    @Override
    public Mono<ResponseEntity<SearchNotificationsResponse>> searchNotificationsFromSenderId(String xPagopaPnUid, Integer size, String nextPagesKey, Mono<PaNotificationsRequest> paNotificationsRequest, final ServerWebExchange exchange){
        return paNotificationsRequest
                .flatMap(notificationsRequest -> this.infoPaService.searchNotificationsFromSenderId(xPagopaPnUid, size, nextPagesKey, notificationsRequest)
                        .map(searchNotificationsResponse -> ResponseEntity.status(HttpStatus.OK).body(searchNotificationsResponse)));
    }

}
