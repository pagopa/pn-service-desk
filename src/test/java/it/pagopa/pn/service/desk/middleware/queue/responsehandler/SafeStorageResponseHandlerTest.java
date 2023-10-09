package it.pagopa.pn.service.desk.middleware.queue.responsehandler;

import it.pagopa.pn.api.dto.events.GenericEventHeader;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationsFileKeyDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperationFileKey;
import it.pagopa.pn.service.desk.middleware.queue.model.InternalEvent;
import it.pagopa.pn.service.desk.middleware.queue.model.InternalEventBody;
import it.pagopa.pn.service.desk.middleware.queue.producer.InternalQueueMomProducer;
import it.pagopa.pn.service.desk.middleware.responsehandler.SafeStorageResponseHandler;
import it.pagopa.pn.service.desk.model.EventTypeEnum;
import it.pagopa.pn.service.desk.utility.Const;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.when;

class SafeStorageResponseHandlerTest {

    private SafeStorageResponseHandler handler;
    private InternalQueueMomProducer internalQueueMomProducer;
    private OperationsFileKeyDAO operationsFileKeyDAO;

    @BeforeEach
    void setup() {
        internalQueueMomProducer = Mockito.mock(InternalQueueMomProducer.class);
        operationsFileKeyDAO = Mockito.mock(OperationsFileKeyDAO.class);
        handler = new SafeStorageResponseHandler(internalQueueMomProducer, operationsFileKeyDAO);
    }

    @Test
    void handleSafeStorageResponseTest() {
        PnServiceDeskOperationFileKey pnServiceDeskOperationFileKey = new PnServiceDeskOperationFileKey();
        pnServiceDeskOperationFileKey.setFileKey("fileKey");
        pnServiceDeskOperationFileKey.setOperationId("1");
        when(operationsFileKeyDAO.getOperationFileKey(Mockito.anyString())).thenReturn(Mono.just(pnServiceDeskOperationFileKey));

        FileDownloadResponse fileDownloadResponse = new FileDownloadResponse();
        fileDownloadResponse.setKey("fileKey");
        handler.handleSafeStorageResponse(fileDownloadResponse);
        Mockito.verify(internalQueueMomProducer, Mockito.times(1)).push(Mockito.any(InternalEvent.class));
    }

}