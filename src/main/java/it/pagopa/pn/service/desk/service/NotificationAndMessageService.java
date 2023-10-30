package it.pagopa.pn.service.desk.service;


import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.SearchNotificationsRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.SearchNotificationsResponse;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.TimelineResponse;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

public interface NotificationAndMessageService {

    Mono<SearchNotificationsResponse> searchNotificationsFromTaxId(String xPagopaPnUid, OffsetDateTime startDate, OffsetDateTime endDate, Integer size, String nextPagesKey, SearchNotificationsRequest searchMessagesRequest);
    Mono<TimelineResponse> getTimelineOfIUN(String xPagopaPnUid, String iun);
}
