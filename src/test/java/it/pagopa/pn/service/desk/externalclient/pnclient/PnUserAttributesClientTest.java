package it.pagopa.pn.service.desk.externalclient.pnclient;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnuserattributes.v1.dto.*;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.userattributes.PnUserAttributesClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;

class PnUserAttributesClientTest extends BaseTest.WithMockServer {

    @Autowired
    private PnUserAttributesClient pnUserAttributesClient;

    private final LegalDigitalAddressDto expected = new LegalDigitalAddressDto();
    private final CourtesyDigitalAddressDto expectedCourtesy = new CourtesyDigitalAddressDto();

    @BeforeEach
    public void setUp(){
        expected.setAddressType(LegalAddressTypeDto.LEGAL);
        expected.setRecipientId("PF-4fc75df3-0913-407e-bdaa-e50329708b7d");
        expected.setSenderId("default");
        expected.setChannelType(LegalChannelTypeDto.PEC);
        expected.setValue("example@pecSuccess.it");

        expectedCourtesy.setAddressType(CourtesyAddressTypeDto.COURTESY);
        expectedCourtesy.setRecipientId("PF-4fc75df3-0913-407e-bdaa-e50329708b7d");
        expectedCourtesy.setSenderId("default");
        expectedCourtesy.setChannelType(CourtesyChannelTypeDto.SMS);
        expectedCourtesy.setValue("example@pecSuccess.it");
    }

    @Test
    void getLegalAddressBySenderTest(){
        Flux<LegalDigitalAddressDto> legalDigitalAddressDtoFlux = this.pnUserAttributesClient.getLegalAddressBySender("PF-4fc75df3-0913-407e-bdaa-e50329708b7d",
                "default");
        LegalDigitalAddressDto legalDigitalAddressDto = legalDigitalAddressDtoFlux.blockFirst();

        Assertions.assertNotNull(legalDigitalAddressDto);
        Assertions.assertEquals(legalDigitalAddressDto.getAddressType(), expected.getAddressType());
        Assertions.assertEquals(legalDigitalAddressDto.getRecipientId(), expected.getRecipientId());
        Assertions.assertEquals(legalDigitalAddressDto.getSenderId(), expected.getSenderId());
        Assertions.assertEquals(legalDigitalAddressDto.getSenderName(), expected.getSenderName());
        Assertions.assertEquals(legalDigitalAddressDto.getChannelType(), expected.getChannelType());
        Assertions.assertEquals(legalDigitalAddressDto.getValue(), expected.getValue());

    }

    @Test
    void getCourtesyAddressBySender(){
        CourtesyDigitalAddressDto actual = this.pnUserAttributesClient
                .getCourtesyAddressBySender("PF-4fc75df3-0913-407e-bdaa-e50329708b7d","default")
                .blockFirst();

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expectedCourtesy.getAddressType(), actual.getAddressType());
        Assertions.assertEquals(expectedCourtesy.getRecipientId(), actual.getRecipientId());
        Assertions.assertEquals(expectedCourtesy.getSenderId(), actual.getSenderId());
        Assertions.assertEquals(expectedCourtesy.getChannelType(), actual.getChannelType());
        Assertions.assertEquals(expectedCourtesy.getValue(), actual.getValue());
    }

}
