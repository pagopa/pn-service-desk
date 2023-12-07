package it.pagopa.pn.service.desk.rest;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.api.NotificationAndMessageApi;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.service.desk.service.NotificationAndMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@RestController
public class NotificationAndMessageController implements NotificationAndMessageApi {
   @Autowired
    private NotificationAndMessageService notificationAndMessageService; //FIXME constructor injection

    @Override
    public Mono<ResponseEntity<SearchNotificationsResponse>> searchNotificationsFromTaxId(String xPagopaPnUid, Integer size, String nextPagesKey, OffsetDateTime startDate, OffsetDateTime endDate, Mono<SearchNotificationsRequest> searchNotificationsRequest, final ServerWebExchange exchange) {
        return searchNotificationsRequest
                .flatMap(searchMessages -> notificationAndMessageService.searchNotificationsFromTaxId(xPagopaPnUid, startDate, endDate, size, nextPagesKey, searchMessages)
                        .map(searchMessagesResponse -> ResponseEntity.status(HttpStatus.OK).body(searchMessagesResponse)));
    }

    @Override
    public Mono<ResponseEntity<TimelineResponse>> getTimelineOfIUN(String xPagopaPnUid, String iun, ServerWebExchange exchange) {
        return notificationAndMessageService.getTimelineOfIUN(xPagopaPnUid, iun)
                .map(timelineResponse -> ResponseEntity.status(HttpStatus.OK).body(timelineResponse));
    }

    @Override
    public Mono<ResponseEntity<DocumentsResponse>> getDocumentsOfIUN(String xPagopaPnUid, String iun, Mono<DocumentsRequest> documentsRequest, ServerWebExchange exchange) {
        return documentsRequest
                .flatMap(request -> notificationAndMessageService.getDocumentsOfIun(iun, request)
                        .map(documentsResponse -> ResponseEntity.status(HttpStatus.OK).body(documentsResponse)));

    }

    @Override
    public Mono<ResponseEntity<NotificationDetailResponse>> getNotificationFromIUN(String xPagopaPnUid, String iun, ServerWebExchange exchange){
        return notificationAndMessageService.getNotificationFromIUN(iun)
                .map(notificationDetailResponse -> ResponseEntity.status(HttpStatus.OK).body(notificationDetailResponse));
    }

    @Override
    public Mono<ResponseEntity<SearchNotificationsResponse>> searchNotificationsAsDelegateFromInternalId(String xPagopaPnUid, String mandateId, String delegateInternalId, Integer size, String nextPagesKey, OffsetDateTime startDate, OffsetDateTime endDate,  final ServerWebExchange exchange){
        return notificationAndMessageService.searchNotificationsAsDelegateFromInternalId(xPagopaPnUid, mandateId, delegateInternalId, size, nextPagesKey, startDate, endDate)
                .map(searchNotificationsResponseResponseEntity -> ResponseEntity.status(HttpStatus.OK).body(searchNotificationsResponseResponseEntity));
    }

}
