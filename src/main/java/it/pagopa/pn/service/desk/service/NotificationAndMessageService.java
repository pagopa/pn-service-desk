package it.pagopa.pn.service.desk.service;


import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface NotificationAndMessageService {

    record DelegateNotificationSearchHeaders(String xPagopaPnUid, String xPagopaPnCxId) {}

    record DelegateNotificationSearchParameters(String mandateId, String delegateInternalId,
                                                RecipientType recipientType, Integer size,
                                                String nextPagesKey, Instant startDate, Instant endDate) {}

    Mono<SearchNotificationsResponse> searchNotificationsFromTaxId(String xPagopaPnUid, String xPagopaPnCxId, Instant startDate, Instant endDate, Integer size, String nextPagesKey, SearchNotificationsRequest searchMessagesRequest);
    Mono<TimelineResponse> getTimelineOfIUN(String xPagopaPnUid, String iun, SearchNotificationsRequest searchNotificationsRequest);
    Mono<DocumentsResponse> getDocumentsOfIun(String xPagopaPnUid, String xPagopaPnCxId, String iun, DocumentsRequest request);
    Mono<NotificationDetailResponse> getNotificationFromIUN(String iun);
    Mono<SearchNotificationsResponse> searchNotificationsAsDelegateFromInternalId(
            DelegateNotificationSearchHeaders headers, DelegateNotificationSearchParameters parameters);
    Mono<NotificationRecipientDetailResponse> getNotificationRecipientDetail(String iun, String recipientTaxId);
}
