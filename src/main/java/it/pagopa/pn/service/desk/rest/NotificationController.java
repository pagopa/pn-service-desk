package it.pagopa.pn.service.desk.rest;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.api.NotificationApi;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationsUnreachableResponse;
import it.pagopa.pn.service.desk.service.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;

    @Override
    public Mono<ResponseEntity<NotificationsUnreachableResponse>> numberOfUnreachableNotifications(String xPagopaPnUid, Mono<NotificationRequest> notificationRequest, ServerWebExchange exchange) {
        return notificationRequest.flatMap(notification -> notificationService.getUnreachableNotification(xPagopaPnUid, notification)
                .map(notificationsUnreachableResponse -> ResponseEntity.status(HttpStatus.OK).body(notificationsUnreachableResponse)));
    }
}
