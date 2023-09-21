package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush;


import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.api.EventComunicationApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.api.PaperNotificationFailedApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.*;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.api.LegalFactsPrivateApi;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
@AllArgsConstructor
public class PnDeliveryPushClientImpl implements PnDeliveryPushClient{
    private PaperNotificationFailedApi notificationFailedApi;
    private LegalFactsPrivateApi legalFactsPrivateApi;
    private EventComunicationApi eventComunicationApi;

    private static final String RADD_TYPE = "__FSU__";


    @Override
    public Flux<ResponsePaperNotificationFailedDtoDto> paperNotificationFailed(String recipientInternalId) {
        return notificationFailedApi.paperNotificationFailed(recipientInternalId, Boolean.TRUE);
    }

    @Override
    public Flux<LegalFactListElementDto> getNotificationLegalFactsPrivate(String recipientInternalId, String iun) {
        return legalFactsPrivateApi.getNotificationLegalFactsPrivate(recipientInternalId,iun, null,null,null);
    }

    @Override
    public Mono<ResponseNotificationViewedDtoDto> notifyNotificationViewed(String iun, PnServiceDeskOperations entity) {
        RequestNotificationViewedDtoDto request = new RequestNotificationViewedDtoDto();
        request.setRecipientType(RecipientTypeDto.PF);
        request.setRecipientInternalId(entity.getRecipientInternalId());
        request.setRaddBusinessTransactionDate(OffsetDateTime.ofInstant(entity.getOperationStartDate(), ZoneOffset.UTC));
        request.setRaddBusinessTransactionId(entity.getOperationId());
        request.setRaddType(RADD_TYPE);
        return eventComunicationApi.notifyNotificationViewed(iun, request);
    }
}
