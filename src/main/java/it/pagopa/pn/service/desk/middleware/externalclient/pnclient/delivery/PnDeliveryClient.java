package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationAttachmentDownloadMetadataResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationSearchResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationV25Dto;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface PnDeliveryClient {

    Mono<SentNotificationV25Dto> getSentNotificationPrivate(String iun);
    Mono<NotificationSearchResponseDto> searchNotificationsPrivate(Instant startDate, Instant endDate, String recipientId, String senderId, String mandateId, String cxType, Integer size, String nextPagesKey);
    Mono<NotificationAttachmentDownloadMetadataResponseDto> getReceivedNotificationDocumentPrivate(String iun, Integer docIdx, String recipientInternalId, String mandateId);

}
