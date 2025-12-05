package it.pagopa.pn.service.desk.middleware.db.dao.impl;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.encryption.DataEncryption;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;

class OperationDAOImplTest extends BaseTest {

    @Autowired
    private OperationDAOImpl operationDAO;

    @MockitoBean
    private DataEncryption dataEncryption;

    private final PnServiceDeskOperations pnServiceDeskOperations = new PnServiceDeskOperations();
    private final PnServiceDeskAddress pnServiceDeskAddress = new PnServiceDeskAddress();

    @BeforeEach
    public void setUp(){
        initialize();
    }

    private void initialize() {
        pnServiceDeskOperations.setOperationId("1234");
        pnServiceDeskAddress.setOperationId("1234");
        operationDAO.createOperationAndAddress(pnServiceDeskOperations,pnServiceDeskAddress).block();
    }

    @Test
    void createOperationAndAddress() {
        Mockito.when(dataEncryption.encode(Mockito.any(), Mockito.any())).thenReturn("returnOk");
        Mockito.when(dataEncryption.decode(Mockito.any())).thenReturn("returnOk");
        assertNotNull(operationDAO.createOperationAndAddress(pnServiceDeskOperations,pnServiceDeskAddress).block());
    }

    @Test
    void searchOperationsFromRecipientInternalId() {
        Mockito.when(dataEncryption.encode(Mockito.any(), Mockito.any())).thenReturn("returnOk");
        Mockito.when(dataEncryption.decode(Mockito.any())).thenReturn("returnOk");
        assertNotNull(operationDAO.searchOperationsFromRecipientInternalId("1234"));
    }

    @Test
    void getByOperationId() {
        Mockito.when(dataEncryption.encode(Mockito.any(), Mockito.any())).thenReturn("returnOk");
        Mockito.when(dataEncryption.decode(Mockito.any())).thenReturn("returnOk");
        assertNotNull(operationDAO.getByOperationId("1234").block());
    }

    @Test
    void getByOperationIdNotPresent() {
        Mockito.when(dataEncryption.encode(Mockito.any(), Mockito.any())).thenReturn("returnOk");
        Mockito.when(dataEncryption.decode(Mockito.any())).thenReturn("returnOk");
        assertNull(operationDAO.getByOperationId("1").block());
    }

    @Test
    void updateEntity() {
        Mockito.when(dataEncryption.encode(Mockito.any(), Mockito.any())).thenReturn("returnOk");
        Mockito.when(dataEncryption.decode(Mockito.any())).thenReturn("returnOk");
        operationDAO.createOperationAndAddress(pnServiceDeskOperations,pnServiceDeskAddress).block();
        pnServiceDeskOperations.setOperationId("12");
        PnServiceDeskOperations updateRequest = this.operationDAO.updateEntity(pnServiceDeskOperations).block();
        assertNotNull(updateRequest);
        assertEquals(updateRequest.getOperationId(), pnServiceDeskOperations.getOperationId());
    }
}