package it.pagopa.pn.service.desk.rest;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.api.NotificationAndMessageApi;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.service.desk.service.NotificationAndMessageService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@RestController
@AllArgsConstructor
public class NotificationAndMessageController implements NotificationAndMessageApi {

    private final NotificationAndMessageService notificationAndMessageService;

    @Override
    public Mono<ResponseEntity<SearchNotificationsResponse>> searchNotificationsFromTaxId(String xPagopaPnUid, OffsetDateTime startDate, OffsetDateTime endDate, Integer size, String nextPagesKey, Mono<SearchNotificationsRequest> searchNotificationsRequest, ServerWebExchange exchange) {
        return searchNotificationsRequest
                .flatMap(searchMessages -> notificationAndMessageService.searchNotificationsFromTaxId(xPagopaPnUid, startDate, endDate, size, nextPagesKey, searchMessages)
                        .map(searchMessagesResponse -> ResponseEntity.status(HttpStatus.OK).body(searchMessagesResponse)));
    }

    @Override
    public Mono<ResponseEntity<TimelineResponse>> getTimelineOfIUN(String xPagopaPnUid, String iun, ServerWebExchange exchange) {
        return notificationAndMessageService.getTimelineOfIUN(xPagopaPnUid, iun, null)
                .map(timelineResponse -> ResponseEntity.status(HttpStatus.OK).body(timelineResponse));
    }

    @Override
    public Mono<ResponseEntity<TimelineResponse>> getTimelineOfIUNAndTaxId(String xPagopaPnUid, String iun, Mono<SearchNotificationsRequest> searchNotificationsRequest, ServerWebExchange exchange) {
        return searchNotificationsRequest
                .flatMap(request -> notificationAndMessageService.getTimelineOfIUN(xPagopaPnUid, iun, request)
                        .map(timelineResponse -> ResponseEntity.status(HttpStatus.OK).body(timelineResponse)));
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
    public Mono<ResponseEntity<SearchNotificationsResponse>> searchNotificationsAsDelegateFromInternalId(String xPagopaPnUid, String mandateId, String delegateInternalId, String recipientType, OffsetDateTime startDate, OffsetDateTime endDate, Integer size, String nextPagesKey, ServerWebExchange exchange) {
        return notificationAndMessageService.searchNotificationsAsDelegateFromInternalId(xPagopaPnUid, mandateId, delegateInternalId, RecipientType.fromValue(recipientType), size, nextPagesKey, startDate, endDate)
                .map(searchNotificationsResponseResponseEntity -> ResponseEntity.status(HttpStatus.OK).body(searchNotificationsResponseResponseEntity));
    }

    @Override
    public Mono<ResponseEntity<NotificationRecipientDetailResponse>> getNotificationRecipientDetail(String xPagopaPnUid, String iun, Mono<NotificationRecipientDetailRequest> notificationRecipientDetailRequest,  final ServerWebExchange exchange) {
        return notificationRecipientDetailRequest
                .flatMap(request -> notificationAndMessageService.getNotificationRecipientDetail(iun, request.getTaxId()))
                .map(ResponseEntity::ok);
    }


    }
