package it.pagopa.pn.service.desk.middleware.queue.consumer.handler;

import it.pagopa.pn.service.desk.LocalStackTestConfig;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.PaperChannelUpdateDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.PrepareEventDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.SendEventDto;
import it.pagopa.pn.service.desk.middleware.responsehandler.PaperChannelResponseHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@FunctionalSpringBootTest
@Import(LocalStackTestConfig.class)
class PaperChannelEventHandlerTestIT {

    @Autowired
    private PaperChannelEventHandler paperChannelEventHandler;

    @MockitoBean
    private PaperChannelResponseHandler handler;

    @Test
    void consumeMessagePrepareOk() {
        PaperChannelUpdateDto paperChannelUpdateDto = new PaperChannelUpdateDto();
        paperChannelUpdateDto.setPrepareEvent(new PrepareEventDto());
        Message<PaperChannelUpdateDto> message = MessageBuilder.withPayload(paperChannelUpdateDto).build();
        paperChannelEventHandler.pnPaperChannelInboundConsumer(handler, message);
        Mockito.verify(handler).handlePreparePaperChannelEventResponse(Mockito.any());
    }

    @Test
    void consumeMessagePrepareKo() {
        PaperChannelUpdateDto paperChannelUpdateDto = new PaperChannelUpdateDto();
        paperChannelUpdateDto.setPrepareEvent(new PrepareEventDto());
        Message<PaperChannelUpdateDto> message = MessageBuilder.withPayload(paperChannelUpdateDto).build();
        Mockito.doThrow(new RuntimeException()).when(handler).handlePreparePaperChannelEventResponse(Mockito.any());
        Assertions.assertThrows(RuntimeException.class,
                                () -> paperChannelEventHandler.pnPaperChannelInboundConsumer(handler, message));
    }

    @Test
    void consumeMessageSendOk() {
        PaperChannelUpdateDto paperChannelUpdateDto = new PaperChannelUpdateDto();
        paperChannelUpdateDto.setSendEvent(new SendEventDto());
        Message<PaperChannelUpdateDto> message = MessageBuilder.withPayload(paperChannelUpdateDto).build();
        paperChannelEventHandler.pnPaperChannelInboundConsumer(handler, message);
        Mockito.verify(handler).handleResultPaperChannelEventResponse(Mockito.any());
    }

    @Test
    void consumeMessageSendKo() {
        PaperChannelUpdateDto paperChannelUpdateDto = new PaperChannelUpdateDto();
        paperChannelUpdateDto.setSendEvent(new SendEventDto());
        Message<PaperChannelUpdateDto> message = MessageBuilder.withPayload(paperChannelUpdateDto).build();
        Mockito.doThrow(new RuntimeException()).when(handler).handleResultPaperChannelEventResponse(Mockito.any());
        Assertions.assertThrows(RuntimeException.class,
                                () -> paperChannelEventHandler.pnPaperChannelInboundConsumer(handler, message));
    }

}