package it.pagopa.pn.service.desk.middleware.db.dao.impl;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.encryption.DataEncryption;
import it.pagopa.pn.service.desk.middleware.db.dao.AddressDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
//import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;

import static org.junit.jupiter.api.Assertions.*;

class AddressDAOImplTest extends BaseTest {

    @Autowired
    private AddressDAO addressDAO;

    @MockitoBean
    private DataEncryption dataEncryption;

    @Test
    void testAddress(){
        PnServiceDeskAddress address = new PnServiceDeskAddress();
        address.setAddress("Via Cristoforo Colombo");
        address.setCap("21047");
        address.setOperationId("1111");
        Mockito.when(dataEncryption.encode(Mockito.any(), Mockito.any())).thenReturn("returnOk");
        Mockito.when(dataEncryption.decode(Mockito.any())).thenReturn("returnOk");
        addressDAO.createWithTransaction(TransactWriteItemsEnhancedRequest.builder(), address);
        PnServiceDeskAddress pnServiceDeskAddress= addressDAO.getAddress("1111").block();
        assertNull(pnServiceDeskAddress);
    }

}