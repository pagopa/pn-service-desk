package it.pagopa.pn.service.desk.middleware.queue.responsehandler;

import it.pagopa.pn.service.desk.action.PreparePaperChannelAction;
import it.pagopa.pn.service.desk.action.ResultPaperChannelAction;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.PrepareEventDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.SendEventDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.StatusCodeEnumDto;
import it.pagopa.pn.service.desk.middleware.responsehandler.PaperChannelResponseHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.time.ZoneOffset;

class PaperChannelResponseHandlerTest {

    private PaperChannelResponseHandler handler;
    private PreparePaperChannelAction prepareEventAction;
    private ResultPaperChannelAction resultEventAction;

    @BeforeEach
    void setup() {
        prepareEventAction = Mockito.mock(PreparePaperChannelAction.class);
        resultEventAction = Mockito.mock(ResultPaperChannelAction.class);
        handler = new PaperChannelResponseHandler(prepareEventAction, resultEventAction);
    }

    @Test
    void handlePreparePaperChannelEventResponseTest() {
        Instant instant = Instant.parse("2023-10-09T16:04:13.913859900Z");
        PrepareEventDto prepareEvent = new PrepareEventDto();
        prepareEvent.setStatusCode(StatusCodeEnumDto.OK);
        prepareEvent.setStatusDateTime(instant);
        prepareEvent.setRequestId("iun_event_idx_0");
        prepareEvent.setStatusDetail("ok");
        handler.handlePreparePaperChannelEventResponse(prepareEvent);

        Mockito.verify(prepareEventAction, Mockito.times(1)).execute(prepareEvent);
    }

    @Test
    void handleResultPaperChannelEventResponseTest() {
        Instant instant = Instant.parse("2023-10-09T16:04:13.913859900Z");
        SendEventDto sendEventDto = new SendEventDto();
        sendEventDto.setStatusCode(StatusCodeEnumDto.OK);
        sendEventDto.setStatusDateTime(instant);
        sendEventDto.setRequestId("iun_event_idx_0");
        sendEventDto.setStatusDetail("ok");
        handler.handleResultPaperChannelEventResponse(sendEventDto);

        Mockito.verify(resultEventAction, Mockito.times(1)).execute(sendEventDto);
    }

}