package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.api.InternalOnlyApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationAttachmentDownloadMetadataResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationSearchResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationV25Dto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.time.Instant;

@Component
@AllArgsConstructor
public class PnDeliveryClientImpl implements PnDeliveryClient{
    private InternalOnlyApi internalOnlyApi;

    @Override
    public Mono<SentNotificationV25Dto> getSentNotificationPrivate(String iun) {
        return internalOnlyApi.getSentNotificationPrivate(iun);
    }

    @Override
    public Mono<NotificationSearchResponseDto> searchNotificationsPrivate(Instant startDate, Instant endDate, String recipientId, String senderId, String mandateId, String cxType, Integer size, String nextPagesKey) {
        return internalOnlyApi.searchNotificationsPrivate(startDate, endDate, recipientId, null, senderId, null, mandateId, cxType, size, nextPagesKey);
    }

    @Override
    public Mono<NotificationAttachmentDownloadMetadataResponseDto> getReceivedNotificationDocumentPrivate(String iun, Integer docIdx, String recipientInternalId, String mandateId) {
        return internalOnlyApi.getReceivedNotificationDocumentPrivate(iun, docIdx, recipientInternalId, mandateId);
    }

    @Override
    public Mono<NotificationAttachmentDownloadMetadataResponseDto> getPresignedUrlPaymentDocument(String iun, String attachmentName, String recipientInternalId, Integer attachmentIdx) {
        return this.internalOnlyApi.getReceivedNotificationAttachmentPrivate(iun, attachmentName, recipientInternalId, null, attachmentIdx);
    }

}
