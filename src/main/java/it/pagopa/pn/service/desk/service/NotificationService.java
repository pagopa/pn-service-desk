package it.pagopa.pn.service.desk.service;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationsUnreachableResponse;
import reactor.core.publisher.Mono;

public interface NotificationService {

    Mono<NotificationsUnreachableResponse> getUnreachableNotification (String xPagopaPnUid, NotificationRequest notificationRequest);

}
