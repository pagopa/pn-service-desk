package it.pagopa.pn.service.desk.externalclient.pnclient;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault.PnDataVaultClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PnDataVaultClientTest extends BaseTest.WithMockServer {

    @Autowired
    private PnDataVaultClient pnDataVaultClient;

    String data = "FRMTTR76M06B715E";

    @Test
    public void anonymized(){

        String string = this.pnDataVaultClient.anonymized(data).block();

        Assertions.assertNotNull(string);
        Assertions.assertEquals("PF-4fc75df3-0913-407e-bdaa-e50329708b7d",string);
    }
}
