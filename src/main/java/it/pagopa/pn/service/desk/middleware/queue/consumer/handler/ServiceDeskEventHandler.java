package it.pagopa.pn.service.desk.middleware.queue.consumer.handler;

import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.annotation.SqsListenerAcknowledgementMode;
import it.pagopa.pn.service.desk.middleware.queue.consumer.AbstractConsumerMessage;
import org.springframework.stereotype.Component;

import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.service.desk.middleware.queue.model.InternalEventBody;
import it.pagopa.pn.service.desk.middleware.responsehandler.InternalEventResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.messaging.Message;

@Component
@AllArgsConstructor
@Slf4j
public class ServiceDeskEventHandler extends AbstractConsumerMessage {
    private InternalEventResponseHandler responseHandler;

    @SqsListener(value = "${pn.service-desk.topics.internal-queue}", acknowledgementMode = SqsListenerAcknowledgementMode.ALWAYS)
    public void validationOperationsInboundConsumer(Message<InternalEventBody> message){
            try {
                initTraceId(message.getHeaders());
                log.info("Handle message from InternalQueue with content {}", message);
                addOperationIdToMdc(message.getPayload().getOperationId());
                responseHandler.handleInternalEventResponse(message.getPayload());
            } catch (Exception ex) {
                log.error("Error in validationOperationsInboundConsumer {}", ex.getMessage());
                throw ex;
            }
    }

    @SqsListener(value = "${pn.service-desk.topics.internal-queue}", acknowledgementMode = SqsListenerAcknowledgementMode.ALWAYS)
    public void notifyDeliveryPushInboundConsumer(Message<InternalEventBody> message){
            try {
                initTraceId(message.getHeaders());
                log.info("Handle message from InternalQueue with content {}", message);
                responseHandler.handleNotifyDeliveryPushEventResponse(message.getPayload());
            } catch (Exception ex) {
                log.error("Error in notifyDeliveryPushInboundConsumer {}", ex.getMessage());
                throw ex;
            }
    }

    public static void addOperationIdToMdc(String operationId) {
        MDC.put(MDCUtils.MDC_PN_CTX_REQUEST_ID, operationId);
    }
}
