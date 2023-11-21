package it.pagopa.pn.service.desk.externalclient.pnclient;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnuserattributes.v1.dto.LegalAddressTypeDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnuserattributes.v1.dto.LegalChannelTypeDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnuserattributes.v1.dto.LegalDigitalAddressDto;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.userattributes.PnUserAttributesClientImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;

class PnUserAttributesClientTest extends BaseTest.WithMockServer {

    @Autowired
    private PnUserAttributesClientImpl pnUserAttributesClient;

    private final LegalDigitalAddressDto expected = new LegalDigitalAddressDto();

    @BeforeEach
    public void setUp(){
        expected.setAddressType(LegalAddressTypeDto.LEGAL);
        expected.setRecipientId("PF-4fc75df3-0913-407e-bdaa-e50329708b7d");
        expected.setSenderId("default");
        expected.setChannelType(LegalChannelTypeDto.PEC);
        expected.setValue("example@pecSuccess.it");
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

}
