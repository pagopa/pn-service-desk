package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationAttachmentDownloadMetadataResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationSearchResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationV21Dto;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

public interface PnDeliveryClient {

    Mono<SentNotificationV21Dto> getSentNotificationPrivate(String iun);
    Mono<NotificationSearchResponseDto> searchNotificationsPrivate(OffsetDateTime startDate, OffsetDateTime endDate, String recipientId, String senderId, Integer size, String nextPagesKey);
    Mono<NotificationAttachmentDownloadMetadataResponseDto> getReceivedNotificationDocumentPrivate(String iun, Integer docIdx, String recipientInternalId, String mandateId);

}
