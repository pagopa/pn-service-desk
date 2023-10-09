package it.pagopa.pn.service.desk.middleware.queue.responsehandler;

import it.pagopa.pn.service.desk.action.NotifyDeliveryPushAction;
import it.pagopa.pn.service.desk.action.ValidationOperationAction;
import it.pagopa.pn.service.desk.middleware.queue.model.InternalEventBody;
import it.pagopa.pn.service.desk.middleware.responsehandler.InternalEventResponseHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class InternalEventResponseHandlerTest {

    private InternalEventResponseHandler handler;
    private ValidationOperationAction validationOperationAction;
    private NotifyDeliveryPushAction notifyDeliveryPushAction;

    @BeforeEach
    void setup() {
        validationOperationAction = Mockito.mock(ValidationOperationAction.class);
        notifyDeliveryPushAction = Mockito.mock(NotifyDeliveryPushAction.class);
        handler = new InternalEventResponseHandler(validationOperationAction, notifyDeliveryPushAction);
    }

    @Test
    void handleInternalEventResponseTest() {
        InternalEventBody internalEventBody = new InternalEventBody();
        internalEventBody.setOperationId("1");
        handler.handleInternalEventResponse(internalEventBody);
        Mockito.verify(validationOperationAction, Mockito.times(1)).execute(internalEventBody.getOperationId());
    }

    @Test
    void handleNotifyDeliveryPushEventResponseTest() {
        InternalEventBody internalEventBody = new InternalEventBody();
        internalEventBody.setOperationId("1");
        handler.handleNotifyDeliveryPushEventResponse(internalEventBody);
        Mockito.verify(notifyDeliveryPushAction, Mockito.times(1)).execute(internalEventBody);
    }

}