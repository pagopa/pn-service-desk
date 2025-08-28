package it.pagopa.pn.service.desk.middleware.queue.consumer.handler;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.dto.CourtesyMessageProgressEventDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.dto.SingleStatusUpdateDto;
import it.pagopa.pn.service.desk.middleware.responsehandler.ExternalChannelResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class ExternalChannelEventHandler {
    private final ExternalChannelResponseHandler externalChannelResponseHandler;
    private CourtesyMessageProgressEventDto CourtesyMessageProgressEventDto;


    public ExternalChannelEventHandler(ExternalChannelResponseHandler externalChannelResponseHandler) {
        this.externalChannelResponseHandler = externalChannelResponseHandler;
    }

    @Bean
    public Consumer<Message<SingleStatusUpdateDto>> pnExtChannelEventInboundConsumer() {
        return message -> {
            try {
                log.debug("Handle message from Result External Channel with content {}", message);

                SingleStatusUpdateDto singleStatusUpdateDto = message.getPayload();
                externalChannelResponseHandler.handleResultExternalChannelEventResponse(singleStatusUpdateDto);
            } catch (Exception ex) {
                throw ex;
            }
        };
    }
}
