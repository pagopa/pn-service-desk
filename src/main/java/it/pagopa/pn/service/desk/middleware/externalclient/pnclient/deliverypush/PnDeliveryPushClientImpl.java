package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush;


import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.api.PaperNotificationFailedApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.LegalFactListElementDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.ResponsePaperNotificationFailedDtoDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.api.LegalFactsPrivateApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
public class PnDeliveryPushClientImpl implements PnDeliveryPushClient{

    @Autowired
    private PaperNotificationFailedApi notificationFailedApi;

    @Autowired
    private LegalFactsPrivateApi legalFactsPrivateApi;


    @Override
    public Flux<ResponsePaperNotificationFailedDtoDto> paperNotificationFailed(String recipientInternalId) {
        return notificationFailedApi.paperNotificationFailed(recipientInternalId, Boolean.TRUE);
    }

    @Override
    public Flux<LegalFactListElementDto> getNotificationLegalFactsPrivate(String recipientInternalId, String iun) {
        return legalFactsPrivateApi.getNotificationLegalFactsPrivate(recipientInternalId,iun, null,null,null);
    }
}
