package it.pagopa.pn.service.desk.middleware.queue.consumer.handler;

import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.service.desk.middleware.queue.model.InternalEventBody;
import it.pagopa.pn.service.desk.middleware.responsehandler.InternalEventResponseHandler;
import it.pagopa.pn.service.desk.model.NotifyEventDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
@AllArgsConstructor
@Slf4j
public class ServiceDeskEventHandler {
    private InternalEventResponseHandler responseHandler;

    @Bean
    public Consumer<Message<InternalEventBody>> validationOperationsInboundConsumer(){
        return message -> {
            try {
                log.info("Handle message from InternalQueue with content {}", message);
                addOperationIdToMdc(message.getPayload().getOperationId());
                responseHandler.handleInternalEventResponse(message.getPayload());
            } catch (Exception ex) {
                throw ex;
            }
        };
    }

    @Bean
    public Consumer<Message<NotifyEventDTO>> notifyDeliveryPushInboundConsumer(){
        return message -> {
            try {
                log.info("Handle message from InternalQueue with content {}", message);
                responseHandler.handleNotifyDeliveryPushEventResponse(message.getPayload());
            } catch (Exception ex) {
                throw ex;
            }
        };
    }

    public static void addOperationIdToMdc(String operationId) {
        MDC.put(MDCUtils.MDC_PN_CTX_REQUEST_ID, operationId);
    }
}
