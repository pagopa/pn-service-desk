package it.pagopa.pn.service.desk.externalclient.pnclient;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.v1.dto.PaSummaryDto;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.externalregistries.ExternalRegistriesClientImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;

class ExternalRegistriesClientTest extends BaseTest.WithMockServer{

    @Autowired
    private ExternalRegistriesClientImpl externalRegistriesClient;

    private final PaSummaryDto expectedResponse = new PaSummaryDto();

    @BeforeEach
    public void initialSetup(){
        expectedResponse.setId("4db741cf-17e1-4751-9b7b-7675ccca472b");
        expectedResponse.setName("Agenzia delle Entrate");
    }

    @Test
    void listOnboardedPaTest(){
        Flux<PaSummaryDto> fluxSummaryDto = this.externalRegistriesClient.listOnboardedPa();
        PaSummaryDto summaryDto = fluxSummaryDto.blockFirst();

        Assertions.assertNotNull(summaryDto);
        Assertions.assertEquals(summaryDto.getId(), expectedResponse.getId());
        Assertions.assertEquals(summaryDto.getName(), expectedResponse.getName());
    }

}
