package it.pagopa.pn.service.desk.middleware.queue.consumer.handler;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.service.desk.LocalStackTestConfig;
import it.pagopa.pn.service.desk.middleware.queue.model.InternalEventBody;
import it.pagopa.pn.service.desk.middleware.responsehandler.InternalEventResponseHandler;
import it.pagopa.pn.service.desk.model.EventTypeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@FunctionalSpringBootTest
@Import(LocalStackTestConfig.class)
class ServiceDeskEventHandlerTestIT {

    @Autowired
    private ServiceDeskEventHandler serviceDeskEventHandler;

    @MockitoBean
    private InternalEventResponseHandler responseHandler;

    @Test
    void consumeMessageWithValidationOperationsEventsTypeOk() {
        InternalEventBody body = createInternalEventBody();
        Message<InternalEventBody> message = MessageBuilder
                .withPayload(body)
                .setHeader("eventType", EventTypeEnum.VALIDATION_OPERATIONS_EVENTS.name())
                .setHeader("aws_messageId", "test-message-id")
                .build();

        serviceDeskEventHandler.internalQueueConsumer(message);

        verify(responseHandler).handleInternalEventResponse(body);
        verify(responseHandler, never()).handleNotifyDeliveryPushEventResponse(any());
    }

    @Test
    void consumeMessageWithValidationOperationsEventsTypeKo() {
        InternalEventBody body = createInternalEventBody();
        Message<InternalEventBody> message = MessageBuilder
                .withPayload(body)
                .setHeader("eventType", EventTypeEnum.VALIDATION_OPERATIONS_EVENTS.name())
                .build();

        doThrow(new RuntimeException("Test exception"))
                .when(responseHandler).handleInternalEventResponse(any());

        assertThrows(RuntimeException.class,
                () -> serviceDeskEventHandler.internalQueueConsumer(message));
        verify(responseHandler).handleInternalEventResponse(body);
    }

    @Test
    void consumeMessageWithNotifyDeliveryPushEventTypeOk() {
        InternalEventBody body = createInternalEventBody();
        Message<InternalEventBody> message = MessageBuilder
                .withPayload(body)
                .setHeader("eventType", EventTypeEnum.NOTIFY_DELIVERY_PUSH.name())
                .setHeader("aws_messageId", "test-message-id")
                .build();

        serviceDeskEventHandler.internalQueueConsumer(message);

        verify(responseHandler).handleNotifyDeliveryPushEventResponse(body);
        verify(responseHandler, never()).handleInternalEventResponse(any());
    }

    @Test
    void consumeMessageWithNotifyDeliveryPushEventTypeKo() {
        InternalEventBody body = createInternalEventBody();
        Message<InternalEventBody> message = MessageBuilder
                .withPayload(body)
                .setHeader("eventType", EventTypeEnum.NOTIFY_DELIVERY_PUSH.name())
                .build();

        doThrow(new RuntimeException("Test exception"))
                .when(responseHandler).handleNotifyDeliveryPushEventResponse(any());

        assertThrows(RuntimeException.class,
                () -> serviceDeskEventHandler.internalQueueConsumer(message));
        verify(responseHandler).handleNotifyDeliveryPushEventResponse(body);
    }

    @Test
    void consumeMessageWithUnsupportedEventTypeThrowsException() {
        InternalEventBody body = createInternalEventBody();
        Message<InternalEventBody> message = MessageBuilder
                .withPayload(body)
                .setHeader("eventType", "UNSUPPORTED_EVENT_TYPE")
                .build();

        PnInternalException exception = assertThrows(PnInternalException.class,
                () -> serviceDeskEventHandler.internalQueueConsumer(message));

        assertNotNull(exception.getProblem().getDetail());
        assertTrue(exception.getProblem().getDetail().contains("eventType not present"));
        verify(responseHandler, never()).handleInternalEventResponse(any());
        verify(responseHandler, never()).handleNotifyDeliveryPushEventResponse(any());
    }

    @Test
    void consumeMessageWithNullEventTypeThrowsException() {
        InternalEventBody body = createInternalEventBody();
        Message<InternalEventBody> message = MessageBuilder
                .withPayload(body)
                .build();

        PnInternalException exception = assertThrows(PnInternalException.class,
                () -> serviceDeskEventHandler.internalQueueConsumer(message));

        assertNotNull(exception.getProblem().getDetail());
        assertTrue(exception.getProblem().getDetail().contains("eventType not present"));
        verify(responseHandler, never()).handleInternalEventResponse(any());
        verify(responseHandler, never()).handleNotifyDeliveryPushEventResponse(any());
    }

    private InternalEventBody createInternalEventBody() {
        InternalEventBody body = new InternalEventBody();
        body.setOperationId("operation-123");
        body.setIuns(Collections.singletonList("iun-001"));
        body.setRecipientInternalId("recipient-456");
        body.setAttempt(1);
        return body;
    }
}
