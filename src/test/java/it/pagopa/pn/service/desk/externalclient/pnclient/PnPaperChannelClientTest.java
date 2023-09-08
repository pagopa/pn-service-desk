package it.pagopa.pn.service.desk.externalclient.pnclient;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.paperchannel.PnPaperChannelClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PnPaperChannelClientTest extends BaseTest.WithMockServer{

    @Autowired
    private PnPaperChannelClient pnPaperChannelClient;

    @Test
    void sendPaperPrepareRequest(){

    }

    @Test
    void sendPaperSendRequest(){

    }
}
