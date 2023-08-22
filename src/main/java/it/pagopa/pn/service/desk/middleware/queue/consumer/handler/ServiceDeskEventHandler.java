package it.pagopa.pn.service.desk.middleware.queue.consumer.handler;

import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.safestorage.PnSafeStorageClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
@AllArgsConstructor
@Slf4j
public class ServiceDeskEventHandler {

    @Bean
    public Consumer<Message<Void>> validationOperationsInboundConsumer(){
        return message -> {
            try {
                log.debug("Handle message from {} with content {}", PnSafeStorageClient.CLIENT_NAME, message);
            } catch (Exception ex) {
                throw ex;
            }
        };
    }
}
