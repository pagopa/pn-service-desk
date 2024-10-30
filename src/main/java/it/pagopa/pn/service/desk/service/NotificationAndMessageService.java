package it.pagopa.pn.service.desk.service;


import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface NotificationAndMessageService {

    Mono<SearchNotificationsResponse> searchNotificationsFromTaxId(String xPagopaPnUid, Instant startDate, Instant endDate, Integer size, String nextPagesKey, SearchNotificationsRequest searchMessagesRequest);
    Mono<TimelineResponse> getTimelineOfIUN(String xPagopaPnUid, String iun, SearchNotificationsRequest searchNotificationsRequest);
    Mono<DocumentsResponse> getDocumentsOfIun(String iun, DocumentsRequest request);
    Mono<NotificationDetailResponse> getNotificationFromIUN(String iun);
    Mono<SearchNotificationsResponse> searchNotificationsAsDelegateFromInternalId(String xPagopaPnUid, String mandateId, String delegateInternalId, RecipientType recipientType, Integer size, String nextPagesKey, Instant startDate, Instant endDate);
    Mono<NotificationRecipientDetailResponse> getNotificationRecipientDetail(String iun, String recipientTaxId);
}
