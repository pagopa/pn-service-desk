package it.pagopa.pn.service.desk.middleware.queue.consumer.handler;

import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.annotation.SqsListenerAcknowledgementMode;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.service.desk.middleware.queue.consumer.AbstractConsumerMessage;
import org.springframework.stereotype.Component;

import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.service.desk.middleware.queue.model.InternalEventBody;
import it.pagopa.pn.service.desk.middleware.responsehandler.InternalEventResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.messaging.Message;

import static it.pagopa.pn.service.desk.exception.PnServiceDeskExceptionCodes.ERROR_CODE_SERVICEDESK_EVENTTYPENOTSUPPORTED;
import static it.pagopa.pn.service.desk.model.EventTypeEnum.NOTIFY_DELIVERY_PUSH;
import static it.pagopa.pn.service.desk.model.EventTypeEnum.VALIDATION_OPERATIONS_EVENTS;

@Component
@AllArgsConstructor
@Slf4j
public class ServiceDeskEventHandler extends AbstractConsumerMessage {
    private InternalEventResponseHandler responseHandler;

    @SqsListener(value = "${pn.service-desk.topics.internal-queue}", acknowledgementMode = SqsListenerAcknowledgementMode.ON_SUCCESS)
    public void internalQueueConsumer(Message<InternalEventBody> message){
        try {
            initTraceId(message.getHeaders());
            log.info("Handle message from InternalQueue with content {}", message);
            addOperationIdToMdc(message.getPayload().getOperationId());
            chooseAndExecuteAction(message);
        } catch (Exception ex) {
            log.error("Error in internalQueueConsumer {}", ex.getMessage());
            throw ex;
        }
    }

    private void chooseAndExecuteAction(Message<InternalEventBody> message) {
        String eventType = (String) message.getHeaders().get("eventType");
        if (VALIDATION_OPERATIONS_EVENTS.name().equals(eventType))
            responseHandler.handleInternalEventResponse(message.getPayload());
        else if (NOTIFY_DELIVERY_PUSH.name().equals(eventType))
            responseHandler.handleNotifyDeliveryPushEventResponse(message.getPayload());
        else {
            log.error("eventType not present, cannot start scheduled action headers={} operationId={}", message.getHeaders(), message.getPayload().getOperationId());
            throw new PnInternalException("eventType not present, cannot start scheduled action", ERROR_CODE_SERVICEDESK_EVENTTYPENOTSUPPORTED);
        }
    }

    private static void addOperationIdToMdc(String operationId) {
        MDC.put(MDCUtils.MDC_PN_CTX_REQUEST_ID, operationId);
    }
}