package it.pagopa.pn.service.desk.rest;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.api.NotificationAndMessageApi;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationsResponse;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.SearchMessagesRequest;
import it.pagopa.pn.service.desk.service.NotificationAndMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class NotificationAndMessageController implements NotificationAndMessageApi {
   @Autowired
    private NotificationAndMessageService notificationAndMessageService;

    @Override
    public Mono<ResponseEntity<NotificationsResponse>> searchCourtesyMessagesFromTaxId(String xPagopaPnUid, Integer size, String nextPagesKey, String startDate, String endDate, Mono<SearchMessagesRequest> searchMessagesRequest, ServerWebExchange exchange) {
        return searchMessagesRequest
                .flatMap(searchMessages -> notificationAndMessageService.searchCourtesyMessagesFromTaxId(xPagopaPnUid, startDate, endDate, size, nextPagesKey, searchMessages)
                        .map(searchMessagesResponse -> ResponseEntity.status(HttpStatus.OK).body(searchMessagesResponse)));
    }
}
