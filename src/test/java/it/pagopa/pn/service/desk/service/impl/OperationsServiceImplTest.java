package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileCreationResponse;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.service.desk.middleware.db.dao.AddressDAO;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationsFileKeyDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperationFileKey;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault.PnDataVaultClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.raddfsu.PnRaddFsuClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.safestorage.PnSafeStorageClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

class OperationsServiceImplTest extends BaseTest {


    @MockBean
    private PnDataVaultClient dataVaultClient;
    @MockBean
    private AddressDAO addressDAO;
    @MockBean
    private PnSafeStorageClient safeStorageClient;
    @MockBean
    private OperationDAO operationDAO;
    @MockBean
    private OperationsFileKeyDAO operationsFileKeyDAO;
    @Autowired
    private OperationsServiceImpl service;

    @Test
    void createOperation() {
        PnServiceDeskAddress pnServiceDeskAddress= new PnServiceDeskAddress();
        PnServiceDeskOperations pnServiceDeskOperations= new PnServiceDeskOperations();

        Mockito.when(dataVaultClient.anonymized(Mockito.any())).thenReturn(Mono.just(new String()));
        Mockito.when(addressDAO.createAddress(Mockito.any())).thenReturn(Mono.just(pnServiceDeskAddress));
        Mockito.when(operationDAO.getByOperationId(Mockito.any())).thenReturn(Mono.just(pnServiceDeskOperations));
        Mockito.when(operationDAO.createOperation(Mockito.any())).thenReturn(Mono.just(pnServiceDeskOperations));

        assertNotNull(service.createOperation("1234", getCreateOperationRequest()));

    }

    @Test
    void searchOperationsFromRecipientInternalId() {
        PnServiceDeskOperations pnServiceDeskOperations= new PnServiceDeskOperations();

        Mockito.when(dataVaultClient.anonymized(Mockito.any())).thenReturn(Mono.just(new String()));
        Mockito.when(operationDAO.searchOperationsFromRecipientInternalId(Mockito.any())).thenReturn(Flux.just(new PnServiceDeskOperations()));

        assertNotNull(service.searchOperationsFromRecipientInternalId("1234", getNotificationRequest()));

    }

    @Test
    void presignedUrlVideoUpload() {
        VideoUploadResponse videoUploadResponse= new VideoUploadResponse();
        PnServiceDeskOperations pnServiceDeskOperations= new PnServiceDeskOperations();

        Mockito.when(safeStorageClient.getPresignedUrl(Mockito.any())).thenReturn(Mono.just(new FileCreationResponse()));
        Mockito.when(operationDAO.getByOperationId(Mockito.any())).thenReturn(Mono.just(pnServiceDeskOperations));
        Mockito.when(operationsFileKeyDAO.updateVideoFileKey(Mockito.any())).thenReturn(Mono.just(new PnServiceDeskOperationFileKey()));
        Mockito.when(operationsFileKeyDAO.getFileKeyByOperationId(Mockito.any())).thenReturn(Mono.just(new PnServiceDeskOperationFileKey()));

        assertNotNull(service.presignedUrlVideoUpload("1234", "1234", getVideoUploadRequest()));


    }



    private CreateOperationRequest getCreateOperationRequest(){
        CreateOperationRequest request = new CreateOperationRequest();
        AnalogAddress analogAddress= new AnalogAddress();
        analogAddress.setAddress("via mario rossi");
        analogAddress.setCap("80080");
        analogAddress.setCity("ferrara");
        analogAddress.setFullname("test");
        request.setTaxId("1234567");
        request.setAddress(analogAddress);
        request.setTicketId("1234");
        request.setTicketOperationId("1234");
        return request;
    }

    private VideoUploadRequest getVideoUploadRequest(){
        VideoUploadRequest request = new VideoUploadRequest();
        request.setPreloadIdx("123");
        request.setContentType("test");
        request.setSha256("1234");
        return request;
    }

    private SearchNotificationRequest getNotificationRequest(){
        SearchNotificationRequest request = new SearchNotificationRequest();
        request.setTaxId("123");
        return request;
    }
}