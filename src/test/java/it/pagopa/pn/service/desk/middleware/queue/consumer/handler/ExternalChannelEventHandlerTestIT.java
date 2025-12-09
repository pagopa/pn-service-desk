package it.pagopa.pn.service.desk.middleware.queue.consumer.handler;

import it.pagopa.pn.service.desk.LocalStackTestConfig;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.dto.SingleStatusUpdateDto;
import it.pagopa.pn.service.desk.middleware.responsehandler.ExternalChannelResponseHandler;
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
class ExternalChannelEventHandlerTestIT {

    @Autowired
    private ExternalChannelEventHandler externalChannelEventHandler;

    @MockitoBean
    private ExternalChannelResponseHandler handler;

    @Test
    void consumeMessageOk() {
        SingleStatusUpdateDto dto = new SingleStatusUpdateDto();
        Message<SingleStatusUpdateDto> message = MessageBuilder.withPayload(dto).build();

        externalChannelEventHandler.pnExtChannelEventInboundConsumer(message);

        Mockito.verify(handler).handleResultExternalChannelEventResponse(dto);
    }

    @Test
    void consumeMessageThrowsException() {
        SingleStatusUpdateDto dto = new SingleStatusUpdateDto();
        Message<SingleStatusUpdateDto> message = MessageBuilder.withPayload(dto).build();

        Mockito.doThrow(new RuntimeException("Test exception"))
               .when(handler).handleResultExternalChannelEventResponse(Mockito.any());

        Assertions.assertThrows(RuntimeException.class, () -> externalChannelEventHandler.pnExtChannelEventInboundConsumer(message));
    }

}