package it.pagopa.pn.service.desk.externalclient.pnclient;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnaddressmanager.v1.dto.AnalogAddressDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnaddressmanager.v1.dto.DeduplicatesResponseDto;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.addressmanager.PnAddressManagerClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;



class PnAddressManagerClientTest extends BaseTest.WithMockServer{

    @Autowired
    private PnAddressManagerClient pnAddressManagerClient;

    private DeduplicatesResponseDto deduplicatesResponseDto = new DeduplicatesResponseDto();
    private AnalogAddressDto normalizedAddress = new AnalogAddressDto();

    @BeforeEach
    public void inititalSetUp(){
        deduplicatesResponseDto.setCorrelationId("1234");
        deduplicatesResponseDto.setEqualityResult(true);
        deduplicatesResponseDto.setError(null);

        normalizedAddress.setAddressRow("Via Milano");
        normalizedAddress.setCap("20089");
        normalizedAddress.setCity("Milano");
        normalizedAddress.setCity2("");
        normalizedAddress.setPr("MI");
        normalizedAddress.setCountry("Italia");

        deduplicatesResponseDto.setNormalizedAddress(normalizedAddress);

    }

    @Test
    public void deduplicates() {
        DeduplicatesResponseDto deduplicate = this.pnAddressManagerClient.deduplicates(new PnServiceDeskAddress()).block();

        Assertions.assertNotNull(deduplicate);
        Assertions.assertEquals(deduplicate.getCorrelationId(),deduplicatesResponseDto.getCorrelationId());
        Assertions.assertEquals(deduplicate.getCorrelationId(), deduplicatesResponseDto.getCorrelationId());
        Assertions.assertEquals(deduplicate.getEqualityResult(), deduplicatesResponseDto.getEqualityResult());
        Assertions.assertEquals(deduplicate.getError(), deduplicatesResponseDto.getError());

        AnalogAddressDto deduplicateNormalizedAddress = deduplicate.getNormalizedAddress();
        AnalogAddressDto responseNormalizedAddress = deduplicatesResponseDto.getNormalizedAddress();

        Assertions.assertEquals(deduplicateNormalizedAddress.getAddressRow(), responseNormalizedAddress.getAddressRow());
        Assertions.assertEquals(deduplicateNormalizedAddress.getCap(), responseNormalizedAddress.getCap());
        Assertions.assertEquals(deduplicateNormalizedAddress.getCity(), responseNormalizedAddress.getCity());
        Assertions.assertEquals(deduplicateNormalizedAddress.getCity2(), responseNormalizedAddress.getCity2());
        Assertions.assertEquals(deduplicateNormalizedAddress.getPr(), responseNormalizedAddress.getPr());
        Assertions.assertEquals(deduplicateNormalizedAddress.getCountry(), responseNormalizedAddress.getCountry());

    }

}
