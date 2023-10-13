package it.pagopa.pn.service.desk.service;


import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationsResponse;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.SearchMessagesRequest;
import reactor.core.publisher.Mono;

public interface NotificationAndMessageService {

    Mono<NotificationsResponse> searchCourtesyMessagesFromTaxId(String xPagopaPnUid, String startDate, String endDate, Integer size, String nextPagesKey, SearchMessagesRequest searchMessagesRequest);
}
