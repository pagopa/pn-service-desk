package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.api.InternalOnlyApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.LegalNotificationSearchResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationAttachmentDownloadMetadataResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationV26Dto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class PnDeliveryClientImpl implements PnDeliveryClient{
    private InternalOnlyApi internalOnlyApi;

    @Override
    public Mono<SentNotificationV26Dto> getSentNotificationPrivate(String iun) {
        return internalOnlyApi.getSentNotificationPrivate(iun);
    }

    @Override
    public Mono<LegalNotificationSearchResponseDto> searchNotificationsPrivate(SearchNotificationsPrivateHeaders headers,
                                                                                 SearchNotificationsPrivateParameters parameters) {
        return internalOnlyApi.searchNotificationsPrivate(parameters.startDate(), parameters.endDate(), parameters.recipientId(), null,
                parameters.senderId(), null, parameters.mandateId(), parameters.cxType(), parameters.size(), parameters.nextPagesKey(),
                headers.xPagopaPnUid(), headers.xPagopaPnCxId(), CxTypeAuthFleetDto.SD);
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
