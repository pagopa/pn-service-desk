package it.pagopa.pn.service.desk.externalclient.pnclient;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.*;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;

class PnDeliveryPushClientTest extends BaseTest.WithMockServer {
    private static final String IUN = "LJLH-GNTJ-DVXR-202209-J-1";
    private static final String IUN1 = "LJLH-GNTJ-DVXR-202209-J-2";
    private static final String RECIPIENT_INTERNAL_ID = "PF-4fc75df3-0913-407e-bdaa-e50329708b7d";

    private static final String RECIPIENT_INTERNAL_ID_ERROR = "PF-4fc75df3-0913-407e-bdaa-e503297";

    private static final String RECIPIENT_INTERNAL_ID_NOT_FOUND = "PF-4fc75df3-0913-407e-bdaa-e50329722";

    private static final String OPERATION_ID = "test12345";

    private final TimelineElementV25Dto expectedTimeline = new TimelineElementV25Dto();

    @Autowired
    private PnDeliveryPushClient pnDeliveryPushClient;

    @BeforeEach
    public void setUp(){
        expectedTimeline.setElementId("SEND_DIGITAL.IUN_PRVZ-NZKM-JEDK-202309-A-1.RECINDEX_0.SOURCE_PLATFORM.REPEAT_false.ATTEMPT_0");
        expectedTimeline.setTimestamp(Instant.parse("2023-09-29T14:04:11.354725545Z"));
        expectedTimeline.setCategory(TimelineElementCategoryV23Dto.SEND_DIGITAL_DOMICILE);

        TimelineElementDetailsV23Dto categoryV23Dto = new TimelineElementDetailsV23Dto();
        categoryV23Dto.setSendDate(Instant.parse("2023-09-29T14:04:01.033478852Z"));
        categoryV23Dto.setDigitalAddressSource(DigitalAddressSourceDto.PLATFORM);
        DigitalAddressDto digitalAddressDto = new DigitalAddressDto();
        digitalAddressDto.setType("PEC");
        digitalAddressDto.setAddress("2c25227d-9835-42a2-a274-a462242a9619@pec.it");
        categoryV23Dto.setDigitalAddress(digitalAddressDto);

        expectedTimeline.setDetails(categoryV23Dto);
    }

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
        List<LegalFactListElementV20Dto> legalFactListElementDtos =
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

    @Test
    void getNotificationHistory(){
        NotificationHistoryResponseDto actual = this.pnDeliveryPushClient
                .getNotificationHistory(
                        "ENEZ-VXZU-JDJQ-202309-L-1",
                        1,
                        Instant.parse("2023-09-29T14:02:08.203039228Z")
                ).block();

        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.getTimeline());
        Assertions.assertNotNull(actual.getTimeline().get(0));

        TimelineElementV25Dto actualTimeline = actual.getTimeline().get(0);
        Assertions.assertEquals(expectedTimeline.getElementId(), actualTimeline.getElementId());
        Assertions.assertEquals(expectedTimeline.getTimestamp(), actualTimeline.getTimestamp());
        Assertions.assertEquals(expectedTimeline.getCategory(), actualTimeline.getCategory());
        Assertions.assertEquals(expectedTimeline.getDetails().getDigitalAddress().getType(), actualTimeline.getDetails().getDigitalAddress().getType());
        Assertions.assertEquals(expectedTimeline.getDetails().getDigitalAddressSource(), actualTimeline.getDetails().getDigitalAddressSource());
        Assertions.assertEquals(expectedTimeline.getDetails().getSendDate(), actualTimeline.getDetails().getSendDate());
    }

}
