package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.api.InternalOnlyApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationAttachmentDownloadMetadataResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationSearchResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationV21Dto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Component
@AllArgsConstructor
public class PnDeliveryClientImpl implements PnDeliveryClient{
    private InternalOnlyApi internalOnlyApi;

    @Override
    public Mono<SentNotificationV21Dto> getSentNotificationPrivate(String iun) {
        return internalOnlyApi.getSentNotificationPrivate(iun);
    }

    @Override
    public Mono<NotificationSearchResponseDto> searchNotificationsPrivate(OffsetDateTime startDate, OffsetDateTime endDate, String recipientId, String senderId, Integer size, String nextPagesKey) {
        return internalOnlyApi.searchNotificationsPrivate(startDate, endDate, recipientId, null, senderId, null, size, nextPagesKey);
    }

    @Override
    public Mono<NotificationAttachmentDownloadMetadataResponseDto> getReceivedNotificationDocumentPrivate(String iun, Integer docIdx, String recipientInternalId, String mandateId) {
        return internalOnlyApi.getReceivedNotificationDocumentPrivate(iun, docIdx, recipientInternalId, mandateId);
    }

}
