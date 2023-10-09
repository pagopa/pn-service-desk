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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Consumer;

@FunctionalSpringBootTest
@Import(LocalStackTestConfig.class)
class PaperChannelEventHandlerTestIT {

    @Autowired
    private FunctionCatalog functionCatalog;

    @MockBean
    private PaperChannelResponseHandler handler;

    @Test
    void consumeMessagePrepareOk() {
        Consumer<Message<PaperChannelUpdateDto>> pnPaperChannelInboundConsumer = functionCatalog.lookup(Consumer.class, "pnPaperChannelInboundConsumer");
        PaperChannelUpdateDto paperChannelUpdateDto = new PaperChannelUpdateDto();
        paperChannelUpdateDto.setPrepareEvent(new PrepareEventDto());
        Message<PaperChannelUpdateDto> message = MessageBuilder.withPayload(paperChannelUpdateDto).build();
        pnPaperChannelInboundConsumer.accept(message);
        Mockito.verify(handler).handlePreparePaperChannelEventResponse(Mockito.any());
    }

    @Test
    void consumeMessagePrepareKo() {
        Consumer<Message<PaperChannelUpdateDto>> pnPaperChannelInboundConsumer = functionCatalog.lookup(Consumer.class, "pnPaperChannelInboundConsumer");
        PaperChannelUpdateDto paperChannelUpdateDto = new PaperChannelUpdateDto();
        paperChannelUpdateDto.setPrepareEvent(new PrepareEventDto());
        Message<PaperChannelUpdateDto> message = MessageBuilder.withPayload(paperChannelUpdateDto).build();
        Mockito.doThrow(new RuntimeException()).when(handler).handlePreparePaperChannelEventResponse(Mockito.any());
        Assertions.assertThrows(RuntimeException.class,
                () -> pnPaperChannelInboundConsumer.accept(message));
    }

    @Test
    void consumeMessageSendOk() {
        Consumer<Message<PaperChannelUpdateDto>> pnPaperChannelInboundConsumer = functionCatalog.lookup(Consumer.class, "pnPaperChannelInboundConsumer");
        PaperChannelUpdateDto paperChannelUpdateDto = new PaperChannelUpdateDto();
        paperChannelUpdateDto.setSendEvent(new SendEventDto());
        Message<PaperChannelUpdateDto> message = MessageBuilder.withPayload(paperChannelUpdateDto).build();
        pnPaperChannelInboundConsumer.accept(message);
        Mockito.verify(handler).handleResultPaperChannelEventResponse(Mockito.any());
    }

    @Test
    void consumeMessageSendKo() {
        Consumer<Message<PaperChannelUpdateDto>> pnPaperChannelInboundConsumer = functionCatalog.lookup(Consumer.class, "pnPaperChannelInboundConsumer");
        PaperChannelUpdateDto paperChannelUpdateDto = new PaperChannelUpdateDto();
        paperChannelUpdateDto.setSendEvent(new SendEventDto());
        Message<PaperChannelUpdateDto> message = MessageBuilder.withPayload(paperChannelUpdateDto).build();
        Mockito.doThrow(new RuntimeException()).when(handler).handleResultPaperChannelEventResponse(Mockito.any());
        Assertions.assertThrows(RuntimeException.class,
                () -> pnPaperChannelInboundConsumer.accept(message));
    }
}
