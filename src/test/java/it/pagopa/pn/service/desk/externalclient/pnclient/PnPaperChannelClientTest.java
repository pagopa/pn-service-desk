package it.pagopa.pn.service.desk.externalclient.pnclient;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.PaperChannelUpdateDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.PrepareRequestDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.SendRequestDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.SendResponseDto;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.paperchannel.PnPaperChannelClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PnPaperChannelClientTest extends BaseTest.WithMockServer{

    @Autowired
    private PnPaperChannelClient pnPaperChannelClient;

    @Test
    void sendPaperPrepareRequest(){
        PaperChannelUpdateDto paperChannelUpdateDto = this.pnPaperChannelClient.sendPaperPrepareRequest("12345", new PrepareRequestDto()).block();
        Assertions.assertNotNull(paperChannelUpdateDto);
    }

    @Test
    void sendPaperSendRequest(){
        SendResponseDto sendResponseDto = this.pnPaperChannelClient.sendPaperSendRequest("4321", new SendRequestDto()).block();
        Assertions.assertNotNull(sendResponseDto);
    }
}
