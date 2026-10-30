package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationAttachmentDownloadMetadataResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.LegalNotificationSearchResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationV26Dto;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface PnDeliveryClient {

    Mono<SentNotificationV26Dto> getSentNotificationPrivate(String iun);
    Mono<LegalNotificationSearchResponseDto> searchNotificationsPrivate(String xPagopaPnUid, String xPagopaPnCxId, Instant startDate, Instant endDate, String recipientId, String senderId, String mandateId, String cxType, Integer size, String nextPagesKey);
    Mono<NotificationAttachmentDownloadMetadataResponseDto> getReceivedNotificationDocumentPrivate(String xPagopaPnUid, String xPagopaPnCxId, String iun, Integer docIdx, String recipientInternalId, String mandateId);
    Mono<NotificationAttachmentDownloadMetadataResponseDto> getPresignedUrlPaymentDocument(String iun, String attachmentName, String recipientTaxId, Integer attachmentIdx);
}
