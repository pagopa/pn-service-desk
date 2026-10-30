package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.api.InternalOnlyApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationAttachmentDownloadMetadataResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.LegalNotificationSearchResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationV26Dto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.CxTypeAuthFleetDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.time.Instant;

@Component
@AllArgsConstructor
public class PnDeliveryClientImpl implements PnDeliveryClient{
    private InternalOnlyApi internalOnlyApi;

    @Override
    public Mono<SentNotificationV26Dto> getSentNotificationPrivate(String iun) {
        return internalOnlyApi.getSentNotificationPrivate(iun);
    }

    @Override
    public Mono<LegalNotificationSearchResponseDto> searchNotificationsPrivate(String xPagopaPnUid, String xPagopaPnCxId, Instant startDate, Instant endDate, String recipientId, String senderId, String mandateId, String cxType, Integer size, String nextPagesKey) {
        return internalOnlyApi.searchNotificationsPrivate(startDate, endDate, recipientId, null, senderId, null, mandateId, cxType, size, nextPagesKey,
                xPagopaPnUid, xPagopaPnCxId, CxTypeAuthFleetDto.SD);
    }

    @Override
    public Mono<NotificationAttachmentDownloadMetadataResponseDto> getReceivedNotificationDocumentPrivate(String xPagopaPnUid, String xPagopaPnCxId, String iun, Integer docIdx, String recipientInternalId, String mandateId) {
        return internalOnlyApi.getReceivedNotificationDocumentPrivate(iun, docIdx, recipientInternalId, mandateId,
                xPagopaPnUid, xPagopaPnCxId, CxTypeAuthFleetDto.SD);
    }

    @Override
    public Mono<NotificationAttachmentDownloadMetadataResponseDto> getPresignedUrlPaymentDocument(String iun, String attachmentName, String recipientInternalId, Integer attachmentIdx) {
        return this.internalOnlyApi.getReceivedNotificationAttachmentPrivate(iun, attachmentName, recipientInternalId, null, attachmentIdx, null, null, CxTypeAuthFleetDto.SD);
    }

}
