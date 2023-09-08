package it.pagopa.pn.service.desk.externalclient.pnclient;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.raddfsu.PnRaddFsuClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PnRaddFsuClientTest extends BaseTest.WithMockServer{

    @Autowired
    private PnRaddFsuClient pnRaddFsuClient;

    @Test
    void aorInquiry(){

    }
}
