package it.pagopa.pn.service.desk.middleware.queue.consumer.handler;

import it.pagopa.pn.service.desk.LocalStackTestConfig;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.service.desk.middleware.responsehandler.SafeStorageResponseHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Consumer;

@FunctionalSpringBootTest
@Import(LocalStackTestConfig.class)
class SafeStorageEventHandlerTestIT {

    @Autowired
    private FunctionCatalog functionCatalog;

    @MockBean
    private SafeStorageResponseHandler handler;

    @Test
    void consumeMessageOk() {
        Consumer<Message<FileDownloadResponse>> pnSafeStorageEventInboundConsumer = functionCatalog.lookup(Consumer.class, "pnSafeStorageEventInboundConsumer");
        FileDownloadResponse fileDownloadResponse = new FileDownloadResponse();
        Message<FileDownloadResponse> message = MessageBuilder.withPayload(fileDownloadResponse).build();
        pnSafeStorageEventInboundConsumer.accept(message);
        Mockito.verify(handler).handleSafeStorageResponse(Mockito.any());
    }

    @Test
    void consumeMessageKo() {
        Consumer<Message<FileDownloadResponse>> pnSafeStorageEventInboundConsumer = functionCatalog.lookup(Consumer.class, "pnSafeStorageEventInboundConsumer");
        FileDownloadResponse fileDownloadResponse = new FileDownloadResponse();
        Message<FileDownloadResponse> message = MessageBuilder.withPayload(fileDownloadResponse).build();
        Mockito.doThrow(new RuntimeException()).when(handler).handleSafeStorageResponse(Mockito.any());
        Assertions.assertThrows(RuntimeException.class,
                () -> pnSafeStorageEventInboundConsumer.accept(message));
    }

}
