package it.pagopa.pn.service.desk.middleware.queue.consumer.handler;

import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.annotation.SqsListenerAcknowledgementMode;
import it.pagopa.pn.service.desk.middleware.queue.consumer.AbstractConsumerMessage;
import org.springframework.stereotype.Component;

import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.safestorage.PnSafeStorageClient;
import it.pagopa.pn.service.desk.middleware.responsehandler.SafeStorageResponseHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.messaging.Message;

@Component
@AllArgsConstructor
@Slf4j
public class SafeStorageEventHandler extends AbstractConsumerMessage {

    private SafeStorageResponseHandler handler;

    @SqsListener(value = "${pn.service-desk.topics.safe-storage-events}", acknowledgementMode = SqsListenerAcknowledgementMode.ON_SUCCESS)
    public void pnSafeStorageEventInboundConsumer(Message<FileDownloadResponse> message)  {
        try {
            initTraceId(message.getHeaders());
            log.debug("Handle message from {} with content {}", PnSafeStorageClient.CLIENT_NAME, message);
            FileDownloadResponse response = message.getPayload();
            MDC.put(MDCUtils.MDC_PN_CTX_SAFESTORAGE_FILEKEY, response.getKey());
            handler.handleSafeStorageResponse(response);
            MDC.remove(MDCUtils.MDC_PN_CTX_SAFESTORAGE_FILEKEY);
        } catch (Exception ex) {
            MDC.remove(MDCUtils.MDC_PN_CTX_SAFESTORAGE_FILEKEY);
            log.error("Error in pnSafeStorageEventInboundConsumer {}", ex.getMessage());
            throw ex;
        }
    }

}