package it.pagopa.pn.service.desk.middleware.queue.consumer.handler;

import it.pagopa.pn.service.desk.LocalStackTestConfig;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.service.desk.middleware.responsehandler.SafeStorageResponseHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.function.Consumer;

@FunctionalSpringBootTest
@Import(LocalStackTestConfig.class)
class SafeStorageEventHandlerTestIT {

    @Autowired
    private SafeStorageEventHandler safeStorageEventHandler;

    @MockitoBean
    private SafeStorageResponseHandler handler;

    @Test
    void consumeMessageOk() {
        FileDownloadResponse fileDownloadResponse = new FileDownloadResponse();
        Message<FileDownloadResponse> message = MessageBuilder.withPayload(fileDownloadResponse).build();
        safeStorageEventHandler.pnSafeStorageEventInboundConsumer(message);
        Mockito.verify(handler).handleSafeStorageResponse(Mockito.any());
    }

    @Test
    void consumeMessageKo() {
        FileDownloadResponse fileDownloadResponse = new FileDownloadResponse();
        Message<FileDownloadResponse> message = MessageBuilder.withPayload(fileDownloadResponse).build();
        Mockito.doThrow(new RuntimeException()).when(handler).handleSafeStorageResponse(Mockito.any());
        Assertions.assertThrows(RuntimeException.class,
                () -> safeStorageEventHandler.pnSafeStorageEventInboundConsumer(message));
    }

}
