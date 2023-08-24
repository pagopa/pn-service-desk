package it.pagopa.pn.service.desk.middleware.queue.consumer.handler;

import it.pagopa.pn.service.desk.middleware.queue.model.InternalEventBody;
import it.pagopa.pn.service.desk.middleware.responsehandler.InternalEventResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
@AllArgsConstructor
@Slf4j
public class ServiceDeskEventHandler {

    @Autowired
    private InternalEventResponseHandler responseHandler;

    @Bean
    public Consumer<Message<InternalEventBody>> validationOperationsInboundConsumer(){
        return message -> {
            try {
                log.debug("Handle message from InternalQueue with content {}", message);
                responseHandler.handleInternalEventResponse(message.getPayload());
            } catch (Exception ex) {
                throw ex;
            }
        };
    }
}
