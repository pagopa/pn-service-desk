package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationAttachmentDownloadMetadataResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.LegalNotificationSearchResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationV26Dto;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface PnDeliveryClient {

    record SearchNotificationsPrivateHeaders(String xPagopaPnUid, String xPagopaPnCxId) {}

    record SearchNotificationsPrivateParameters(Instant startDate, Instant endDate, String recipientId,
                                                String senderId, String mandateId, String cxType,
                                                Integer size, String nextPagesKey) {}

    Mono<SentNotificationV26Dto> getSentNotificationPrivate(String iun);
    Mono<LegalNotificationSearchResponseDto> searchNotificationsPrivate(SearchNotificationsPrivateHeaders headers,
                                                                         SearchNotificationsPrivateParameters parameters);
    Mono<NotificationAttachmentDownloadMetadataResponseDto> getReceivedNotificationDocumentPrivate(String xPagopaPnUid, String xPagopaPnCxId, String iun, Integer docIdx, String recipientInternalId, String mandateId);
    Mono<NotificationAttachmentDownloadMetadataResponseDto> getPresignedUrlPaymentDocument(String iun, String attachmentName, String recipientTaxId, Integer attachmentIdx, String cxOperatorId);
}
