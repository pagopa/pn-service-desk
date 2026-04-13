package it.pagopa.pn.service.desk.middleware.queue.consumer.handler;

import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.annotation.SqsListenerAcknowledgementMode;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.dto.SingleStatusUpdateDto;
import it.pagopa.pn.service.desk.middleware.queue.consumer.AbstractConsumerMessage;
import it.pagopa.pn.service.desk.middleware.responsehandler.ExternalChannelResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class ExternalChannelEventHandler extends AbstractConsumerMessage {

    private final ExternalChannelResponseHandler externalChannelResponseHandler;

    @SqsListener(value = "${pn.service-desk.topics.externalchannel-queue}", acknowledgementMode = SqsListenerAcknowledgementMode.ON_SUCCESS)
    public void pnExtChannelEventInboundConsumer(Message<SingleStatusUpdateDto> message) {
        try {
            initTraceId(message.getHeaders());
            log.debug("Handle message from Result External Channel with content {}", message);
            SingleStatusUpdateDto singleStatusUpdateDto = message.getPayload();
            externalChannelResponseHandler.handleResultExternalChannelEventResponse(singleStatusUpdateDto);
        } catch (Exception ex) {
            log.error("Error in pnExtChannelEventInboundConsumer {}", ex.getMessage());
            throw ex;
        }
    }
}