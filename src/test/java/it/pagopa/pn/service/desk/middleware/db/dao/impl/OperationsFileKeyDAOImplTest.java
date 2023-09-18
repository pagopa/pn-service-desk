package it.pagopa.pn.service.desk.middleware.db.dao.impl;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperationFileKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class OperationsFileKeyDAOImplTest extends BaseTest {

    @Autowired
    private OperationsFileKeyDAOImpl operationsFileKeyDAO;

    private final PnServiceDeskOperationFileKey pnServiceDeskOperationFileKey = new PnServiceDeskOperationFileKey();

    @BeforeEach
    public void setUp(){
        initialize();
    }

    private void initialize() {
        pnServiceDeskOperationFileKey.setOperationId("1234");
        pnServiceDeskOperationFileKey.setFileKey("1234");
        operationsFileKeyDAO.updateVideoFileKey(pnServiceDeskOperationFileKey).block();
    }

    @Test
    void updateVideoFileKey() {
        assertNotNull(operationsFileKeyDAO.updateVideoFileKey(pnServiceDeskOperationFileKey).block());
    }

    @Test
    void getOperationFileKey() {
         assertNotNull(operationsFileKeyDAO.getOperationFileKey("1234").block());
    }

    @Test
    void getFileKeyByOperationId() {
       assertNotNull(operationsFileKeyDAO.getFileKeyByOperationId("1234").block());
    }

    @Test
    void getFileKeyByOperationIdNotPresent() {
        assertNull(operationsFileKeyDAO.getFileKeyByOperationId("1").block());
    }


}