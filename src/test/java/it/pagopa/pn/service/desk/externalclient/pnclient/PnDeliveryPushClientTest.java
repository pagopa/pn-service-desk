package it.pagopa.pn.service.desk.externalclient.pnclient;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.LegalFactListElementDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.ResponsePaperNotificationFailedDtoDto;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;

import java.util.List;

class PnDeliveryPushClientTest extends BaseTest.WithMockServer {
    private static final String IUN = "LJLH-GNTJ-DVXR-202209-J-1";
    private static final String RECIPIENT_INTERNAL_ID = "PF-4fc75df3-0913-407e-bdaa-e50329708b7d";

    @Autowired
    private PnDeliveryPushClient pnDeliveryPushClient;

    @Test
    void paperNotificationFailed(){
        List<ResponsePaperNotificationFailedDtoDto> responsePaperNotificationFailedDtoDto =
                this.pnDeliveryPushClient.paperNotificationFailed(RECIPIENT_INTERNAL_ID).collectList().block();

        Assertions.assertNotNull(responsePaperNotificationFailedDtoDto);
        Assertions.assertEquals(3, responsePaperNotificationFailedDtoDto.size());
    }

    @Test
    void getNotificationLegalFactsPrivate(){


        List<LegalFactListElementDto> legalFactListElementDtos =
                this.pnDeliveryPushClient
                        .getNotificationLegalFactsPrivate(RECIPIENT_INTERNAL_ID, IUN).collectList().block();

        Assertions.assertNotNull(legalFactListElementDtos);
        Assertions.assertEquals(3, legalFactListElementDtos.size());
    }
}
