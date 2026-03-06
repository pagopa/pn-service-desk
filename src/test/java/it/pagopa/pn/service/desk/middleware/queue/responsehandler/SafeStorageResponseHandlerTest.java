package it.pagopa.pn.service.desk.middleware.queue.responsehandler;

import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationsFileKeyDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperationFileKey;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.queue.model.InternalEvent;
import it.pagopa.pn.service.desk.middleware.queue.producer.InternalQueueMomProducer;
import it.pagopa.pn.service.desk.middleware.responsehandler.SafeStorageResponseHandler;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.util.List;

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

    @Test
    void handleSafeStorageResponseTest() {
        PnServiceDeskOperationFileKey pnServiceDeskOperationFileKey = new PnServiceDeskOperationFileKey();
        pnServiceDeskOperationFileKey.setFileKey("fileKey");
        pnServiceDeskOperationFileKey.setOperationId("1");
        when(operationsFileKeyDAO.getOperationFileKey(Mockito.anyString())).thenReturn(Mono.just(pnServiceDeskOperationFileKey));

        PnServiceDeskOperations legacyOp = new PnServiceDeskOperations();
        legacyOp.setOperationId("1");
        when(operationDAO.getByOperationId("1")).thenReturn(Mono.just(legacyOp));

        FileDownloadResponse fileDownloadResponse = new FileDownloadResponse();
        fileDownloadResponse.setKey("fileKey");
        handler.handleSafeStorageResponse(fileDownloadResponse);
        Mockito.verify(internalQueueMomProducer, Mockito.times(1)).push(Mockito.any(InternalEvent.class));
    }

    @Test
    void handleSafeStorageResponse_parentWithSubOps() {
        PnServiceDeskOperationFileKey fileKey = new PnServiceDeskOperationFileKey();
        fileKey.setFileKey("fileKey");
        fileKey.setOperationId("parentId");
        when(operationsFileKeyDAO.getOperationFileKey("fileKey")).thenReturn(Mono.just(fileKey));

        PnServiceDeskOperations parentOp = new PnServiceDeskOperations();
        parentOp.setOperationId("parentId");
        parentOp.setSubOperationsIds(List.of("SUB#parentId#IUN-001", "SUB#parentId#IUN-002"));
        when(operationDAO.getByOperationId("parentId")).thenReturn(Mono.just(parentOp));

        PnServiceDeskOperations subOp1 = new PnServiceDeskOperations();
        subOp1.setOperationId("SUB#parentId#IUN-001");
        subOp1.setStatus(OperationStatusEnum.CREATING.name());
        when(operationDAO.getByOperationId("SUB#parentId#IUN-001")).thenReturn(Mono.just(subOp1));

        PnServiceDeskOperations subOp2 = new PnServiceDeskOperations();
        subOp2.setOperationId("SUB#parentId#IUN-002");
        subOp2.setStatus(OperationStatusEnum.CREATING.name());
        when(operationDAO.getByOperationId("SUB#parentId#IUN-002")).thenReturn(Mono.just(subOp2));

        FileDownloadResponse fileDownloadResponse = new FileDownloadResponse();
        fileDownloadResponse.setKey("fileKey");
        handler.handleSafeStorageResponse(fileDownloadResponse);

        Mockito.verify(internalQueueMomProducer, Mockito.times(2)).push(Mockito.any(InternalEvent.class));
    }

    @Test
    void handleSafeStorageResponse_parentWithSubOps_skipsNonCreating() {
        PnServiceDeskOperationFileKey fileKey = new PnServiceDeskOperationFileKey();
        fileKey.setFileKey("fileKey");
        fileKey.setOperationId("parentId");
        when(operationsFileKeyDAO.getOperationFileKey("fileKey")).thenReturn(Mono.just(fileKey));

        PnServiceDeskOperations parentOp = new PnServiceDeskOperations();
        parentOp.setOperationId("parentId");
        parentOp.setSubOperationsIds(List.of("SUB#parentId#IUN-001", "SUB#parentId#IUN-002"));
        when(operationDAO.getByOperationId("parentId")).thenReturn(Mono.just(parentOp));

        PnServiceDeskOperations subOp1 = new PnServiceDeskOperations();
        subOp1.setOperationId("SUB#parentId#IUN-001");
        subOp1.setStatus(OperationStatusEnum.CREATING.name());
        when(operationDAO.getByOperationId("SUB#parentId#IUN-001")).thenReturn(Mono.just(subOp1));

        PnServiceDeskOperations subOp2 = new PnServiceDeskOperations();
        subOp2.setOperationId("SUB#parentId#IUN-002");
        subOp2.setStatus(OperationStatusEnum.KO.name());
        when(operationDAO.getByOperationId("SUB#parentId#IUN-002")).thenReturn(Mono.just(subOp2));

        FileDownloadResponse fileDownloadResponse = new FileDownloadResponse();
        fileDownloadResponse.setKey("fileKey");
        handler.handleSafeStorageResponse(fileDownloadResponse);

        Mockito.verify(internalQueueMomProducer, Mockito.times(1)).push(Mockito.any(InternalEvent.class));
    }

}
