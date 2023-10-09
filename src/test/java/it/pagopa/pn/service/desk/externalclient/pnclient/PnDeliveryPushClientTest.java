package it.pagopa.pn.service.desk.externalclient.pnclient;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.LegalFactListElementDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.ResponseNotificationViewedDtoDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.ResponsePaperNotificationFailedDtoDto;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.test.StepVerifier;

import java.util.List;

class PnDeliveryPushClientTest extends BaseTest.WithMockServer {
    private static final String IUN = "LJLH-GNTJ-DVXR-202209-J-1";
    private static final String IUN1 = "LJLH-GNTJ-DVXR-202209-J-2";
    private static final String RECIPIENT_INTERNAL_ID = "PF-4fc75df3-0913-407e-bdaa-e50329708b7d";

    private static final String RECIPIENT_INTERNAL_ID_ERROR = "PF-4fc75df3-0913-407e-bdaa-e503297";

    private static final String RECIPIENT_INTERNAL_ID_NOT_FOUND = "PF-4fc75df3-0913-407e-bdaa-e50329722";

    private static final String OPERATION_ID = "test12345";

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
    void paperNotificationFailedException(){
        StepVerifier.create(this.pnDeliveryPushClient.paperNotificationFailed(RECIPIENT_INTERNAL_ID_ERROR))
                .expectError(WebClientResponseException.class)
                .verify();
    }

    @Test
    void paperNotificationFailedExceptionNotFound(){
        List<ResponsePaperNotificationFailedDtoDto> responsePaperNotificationFailedDtoDto =
                this.pnDeliveryPushClient.paperNotificationFailed(RECIPIENT_INTERNAL_ID_NOT_FOUND).collectList().block();

        Assertions.assertNotNull(responsePaperNotificationFailedDtoDto);
        Assertions.assertEquals(0, responsePaperNotificationFailedDtoDto.size());
    }

    @Test
    void getNotificationLegalFactsPrivate(){
        List<LegalFactListElementDto> legalFactListElementDtos =
                this.pnDeliveryPushClient
                        .getNotificationLegalFactsPrivate(RECIPIENT_INTERNAL_ID, IUN).collectList().block();

        Assertions.assertNotNull(legalFactListElementDtos);
        Assertions.assertEquals(3, legalFactListElementDtos.size());
    }

    @Test
    void notifyNotificationViewed(){


        ResponseNotificationViewedDtoDto responseNotificationViewedDtoDto =
                this.pnDeliveryPushClient
                        .notifyNotificationViewed(IUN, OPERATION_ID, RECIPIENT_INTERNAL_ID).block();

        Assertions.assertNotNull(responseNotificationViewedDtoDto);
        Assertions.assertEquals(IUN, responseNotificationViewedDtoDto.getIun());
    }

    @Test
    void notifyNotificationViewedError(){


        ResponseNotificationViewedDtoDto responseNotificationViewedDtoDto =
                this.pnDeliveryPushClient
                        .notifyNotificationViewed(IUN1, OPERATION_ID, RECIPIENT_INTERNAL_ID).block();

        Assertions.assertNull(responseNotificationViewedDtoDto);
    }
}
