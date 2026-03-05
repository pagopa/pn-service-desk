package it.pagopa.pn.service.desk.middleware.queue.responsehandler;

import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationsFileKeyDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperationFileKey;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.queue.model.InternalEvent;
import it.pagopa.pn.service.desk.middleware.queue.producer.InternalQueueMomProducer;
import it.pagopa.pn.service.desk.middleware.responsehandler.SafeStorageResponseHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;

class SafeStorageResponseHandlerTest {

    private SafeStorageResponseHandler handler;
    private InternalQueueMomProducer internalQueueMomProducer;
    private OperationsFileKeyDAO operationsFileKeyDAO;
    private OperationDAO operationDAO;

    @BeforeEach
    void setup() {
        internalQueueMomProducer = Mockito.mock(InternalQueueMomProducer.class);
        operationsFileKeyDAO = Mockito.mock(OperationsFileKeyDAO.class);
        operationDAO = Mockito.mock(OperationDAO.class);
        handler = new SafeStorageResponseHandler(internalQueueMomProducer, operationsFileKeyDAO, operationDAO);
    }

    @ParameterizedTest
    @MethodSource("operations")
    void handleSafeStorageResponseTest(PnServiceDeskOperations operation, int expectedPushes) {
        PnServiceDeskOperationFileKey pnServiceDeskOperationFileKey = new PnServiceDeskOperationFileKey();
        pnServiceDeskOperationFileKey.setFileKey("fileKey");
        pnServiceDeskOperationFileKey.setOperationId("operationId");
        when(operationsFileKeyDAO.getOperationFileKey(Mockito.anyString())).thenReturn(Mono.just(pnServiceDeskOperationFileKey));
        when(operationDAO.getByOperationId(Mockito.anyString())).thenReturn(Mono.just(operation));

        FileDownloadResponse fileDownloadResponse = new FileDownloadResponse();
        fileDownloadResponse.setKey("fileKey");
        handler.handleSafeStorageResponse(fileDownloadResponse);
        Mockito.verify(internalQueueMomProducer, Mockito.times(expectedPushes)).push(Mockito.any(InternalEvent.class));
    }

    static Stream<Arguments> operations() {
        PnServiceDeskOperations op1 = new PnServiceDeskOperations();
        op1.setOperationId("operationId");
        op1.setIun("Iun");
        op1.setIsSubOperation(false);

        PnServiceDeskOperations op2 = new PnServiceDeskOperations();
        op2.setOperationId("operationId");
        op2.setIun(null);
        List<String> ids = List.of("1","2","3");
        op2.setSubOperationsIds(ids);

        return Stream.of(Arguments.of(op1, 1), Arguments.of(op2, 3));
    }

}