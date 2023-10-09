package it.pagopa.pn.service.desk.externalclient.pnclient;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault.PnDataVaultClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClientResponseException;

class PnDataVaultClientTest extends BaseTest.WithMockServer {

    @Autowired
    private PnDataVaultClient pnDataVaultClient;

    String data = "FRMTTR76M06B715E";
    String anonymizedData = "PF-1a434901-5964-4df6-8eff-aa204cb961dz";

    @Test
    void anonymized(){
        String string = this.pnDataVaultClient.anonymized(data).block();

        Assertions.assertNotNull(string);
        Assertions.assertEquals(anonymizedData,string);
    }

    @Test
    void anonymizedError(){
        Assertions.assertThrows(PnGenericException.class, () ->
                this.pnDataVaultClient.anonymized("FRMTTR76M06B715F").block());
    }

    @Test
    void deanonymized(){
        String string = this.pnDataVaultClient.deAnonymized(anonymizedData).block();

        Assertions.assertNotNull(string);
        Assertions.assertEquals(data,string);
    }

    @Test
    void deanonymizedError(){
        String recipentInternalId = anonymizedData.concat("XX");
        Assertions.assertThrows(PnGenericException.class, () ->
                this.pnDataVaultClient.deAnonymized(recipentInternalId).block());
    }
}
