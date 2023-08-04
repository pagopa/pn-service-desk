package it.pagopa.pn.service.desk.middleware.queue.consumer.handler;

import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.safestorage.PnSafeStorageClient;
import it.pagopa.pn.service.desk.middleware.responsehandler.SafeStorageResponseHandler;
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
public class SafeStorageEventHandler {

    private SafeStorageResponseHandler handler;
    
    @Bean
    public Consumer<Message<FileDownloadResponse>> pnSafeStorageEventInboundConsumer() {
        return message -> {
            try {
                log.debug("Handle message from {} with content {}", PnSafeStorageClient.CLIENT_NAME, message);
                FileDownloadResponse response = message.getPayload();
                MDC.put(MDCUtils.MDC_PN_CTX_SAFESTORAGE_FILEKEY, response.getKey());
                //TODO
                handler.handleSafeStorageResponse(response);
                MDC.remove(MDCUtils.MDC_PN_CTX_SAFESTORAGE_FILEKEY);
            } catch (Exception ex) {
                MDC.remove(MDCUtils.MDC_PN_CTX_SAFESTORAGE_FILEKEY);
                throw ex;
            }
        };
    }
   
}
