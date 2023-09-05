package it.pagopa.pn.service.desk.middleware.queue.consumer.handler;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.PaperChannelUpdateDto;
import it.pagopa.pn.service.desk.middleware.responsehandler.PaperChannelResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;


@Configuration
@Slf4j
public class PaperChannelEventHandler {

    @Bean
    public Consumer<Message<PaperChannelUpdateDto>> pnPreparePaperChannelInboundConsumer(PaperChannelResponseHandler responseHandler) {
        return message -> {
            try {
                log.debug("Handle message from Prepare Paper Channel with content {}", message);
                if (message.getPayload().getPrepareEvent() != null){
                    responseHandler.handlePreparePaperChannelEventResponse(message.getPayload().getPrepareEvent());
                } else {
                    log.error("Field of payload is empty");
                }
            } catch (Exception ex) {
                throw ex;
            }
        };
    }

    @Bean
    public Consumer<Message<PaperChannelUpdateDto>> pnSendPaperChannelInboundConsumer(PaperChannelResponseHandler responseHandler) {
        return message -> {
            try {
                log.debug("Handle message from Send Paper Channel with content {}", message);
                if (message.getPayload().getSendEvent() != null) {
                    responseHandler.handleResultPaperChannelEventResponse(message.getPayload().getSendEvent());
                } else {
                    log.error("Field of payload is empty");
                }
            } catch (Exception ex) {
                throw ex;
            }
        };
    }

}
