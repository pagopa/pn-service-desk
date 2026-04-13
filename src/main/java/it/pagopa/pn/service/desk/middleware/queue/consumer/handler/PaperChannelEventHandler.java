package it.pagopa.pn.service.desk.middleware.queue.consumer.handler;

import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.annotation.SqsListenerAcknowledgementMode;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.PaperChannelUpdateDto;
import it.pagopa.pn.service.desk.middleware.queue.consumer.AbstractConsumerMessage;
import it.pagopa.pn.service.desk.middleware.responsehandler.PaperChannelResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class PaperChannelEventHandler extends AbstractConsumerMessage {

    private final PaperChannelResponseHandler responseHandler;

    @SqsListener(value = "${pn.service-desk.topics.paperchannel-queue}", acknowledgementMode = SqsListenerAcknowledgementMode.ON_SUCCESS)
    public void pnPaperChannelInboundConsumer(Message<PaperChannelUpdateDto> message) {
        try {
            initTraceId(message.getHeaders());
            log.debug("Handle message from Prepare Paper Channel with content {}", message);
            if (message.getPayload().getPrepareEvent() != null){
                responseHandler.handlePreparePaperChannelEventResponse(message.getPayload().getPrepareEvent());
            } else if (message.getPayload().getSendEvent() != null) {
                responseHandler.handleResultPaperChannelEventResponse(message.getPayload().getSendEvent());
            } else {
                log.error("Field of payload is empty");
            }
        } catch (Exception ex) {
            log.error("Error in pnPaperChannelInboundConsumer {}", ex.getMessage());
            throw ex;
        }
    }

}