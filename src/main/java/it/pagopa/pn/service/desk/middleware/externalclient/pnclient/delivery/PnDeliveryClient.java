package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationSearchResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationDto;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

public interface PnDeliveryClient {

    Mono<SentNotificationDto> getSentNotificationPrivate(String iun);
    Mono<NotificationSearchResponseDto> searchNotificationsPrivate(OffsetDateTime startDate, OffsetDateTime endDate, String recipientId, String senderId, Integer size, String nextPagesKey);
}
