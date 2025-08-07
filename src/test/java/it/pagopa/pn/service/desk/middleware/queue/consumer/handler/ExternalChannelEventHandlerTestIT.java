package it.pagopa.pn.service.desk.middleware.queue.consumer.handler;

import it.pagopa.pn.service.desk.LocalStackTestConfig;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.dto.CourtesyMessageProgressEventDto;
import it.pagopa.pn.service.desk.middleware.responsehandler.ExternalChannelResponseHandler;
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
class ExternalChannelEventHandlerTestIT {

    @Autowired
    private FunctionCatalog functionCatalog;

    @MockBean
    private ExternalChannelResponseHandler handler;

    @Test
    void consumeMessageOk() {
        // Arrange
        Consumer<Message<CourtesyMessageProgressEventDto>> consumer = functionCatalog.lookup(Consumer.class, "pnExtChannelEventInboundConsumer");

        CourtesyMessageProgressEventDto dto = new CourtesyMessageProgressEventDto();
        Message<CourtesyMessageProgressEventDto> message = MessageBuilder.withPayload(dto).build();

        // Act
        consumer.accept(message);

        // Assert
        Mockito.verify(handler).handleResultExternalChannelEventResponse(Mockito.eq(dto));
    }

    @Test
    void consumeMessageThrowsException() {
        // Arrange
        Consumer<Message<CourtesyMessageProgressEventDto>> consumer = functionCatalog.lookup(Consumer.class, "pnExtChannelEventInboundConsumer");

        CourtesyMessageProgressEventDto dto = new CourtesyMessageProgressEventDto();
        Message<CourtesyMessageProgressEventDto> message = MessageBuilder.withPayload(dto).build();

        Mockito.doThrow(new RuntimeException("Test exception"))
               .when(handler).handleResultExternalChannelEventResponse(Mockito.any());

        // Assert
        Assertions.assertThrows(RuntimeException.class, () -> consumer.accept(message));
    }
}
