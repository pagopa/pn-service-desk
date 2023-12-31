package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.exception.PnRetryStorageException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileCreationResponse;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.service.desk.middleware.db.dao.AddressDAO;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationsFileKeyDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperationFileKey;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault.PnDataVaultClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.safestorage.PnSafeStorageClient;
import it.pagopa.pn.service.desk.service.NotificationService;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Array;
import java.time.Instant;
import java.util.ArrayList;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.*;
import static org.junit.jupiter.api.Assertions.*;

class OperationsServiceImplTest extends BaseTest {
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private PnDataVaultClient dataVaultClient;
    @MockBean
    private PnSafeStorageClient safeStorageClient;
    @MockBean
    private OperationDAO operationDAO;
    @MockBean
    private OperationsFileKeyDAO operationsFileKeyDAO;
    @Autowired
    private OperationsServiceImpl service;


    private final PnServiceDeskOperations pnServiceDeskOperations =new PnServiceDeskOperations();
    private final PnServiceDeskOperationFileKey pnServiceDeskOperationFileKey= new PnServiceDeskOperationFileKey();
    private final FileCreationResponse fileCreationResponse=new FileCreationResponse();


    @BeforeEach
    void inizialize(){
        pnServiceDeskOperations.setOperationId("123");
        pnServiceDeskOperations.setOperationStartDate(Instant.now());
        pnServiceDeskOperations.setOperationLastUpdateDate(Instant.now());
        pnServiceDeskOperations.setStatus("OK");
        pnServiceDeskOperations.setRecipientInternalId("1234");

        pnServiceDeskOperationFileKey.setOperationId("1234");
        pnServiceDeskOperationFileKey.setFileKey("1234");

        fileCreationResponse.setKey("123");
        fileCreationResponse.setUploadUrl("test");
        fileCreationResponse.setSecret("secret");

        Mockito.when(dataVaultClient.anonymized(Mockito.any())).thenReturn(Mono.just("abcdefghilmno"));
        Mockito.when(operationDAO.getByOperationId(Mockito.any())).thenReturn(Mono.just(pnServiceDeskOperations));
        Mockito.when(operationsFileKeyDAO.getFileKeyByOperationId(Mockito.any())).thenReturn(Mono.just(pnServiceDeskOperationFileKey));
    }

    @Test
    void createOperationTestCaseWithNotification() {
        NotificationsUnreachableResponse notificationsUnreachableResponse = new NotificationsUnreachableResponse();
        notificationsUnreachableResponse.setNotificationsCount(1L);

        Mockito.when(notificationService.getUnreachableNotification(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(notificationsUnreachableResponse));
        Mockito.when(operationDAO.getByOperationId(Mockito.any()))
                .thenReturn(Mono.empty());
        Mockito.when(operationDAO.createOperationAndAddress(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(Tuples.of(pnServiceDeskOperations, new PnServiceDeskAddress())));

        assertNotNull(service.createOperation("1234", getCreateOperationRequest()).block());
    }

    @Test
    void createOperationTestCaseWithoutNotification() {
        NotificationsUnreachableResponse notificationsUnreachableResponse = new NotificationsUnreachableResponse();
        notificationsUnreachableResponse.setNotificationsCount(0L);

        Mockito.when(notificationService.getUnreachableNotification(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(notificationsUnreachableResponse));
        Mockito.when(operationDAO.getByOperationId(Mockito.any()))
                .thenReturn(Mono.empty());
        Mockito.when(operationDAO.createOperationAndAddress(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(Tuples.of(pnServiceDeskOperations, new PnServiceDeskAddress())));

        StepVerifier.create(service.createOperation("1234", getCreateOperationRequest()))
                .expectError(PnGenericException.class)
                .verify();
    }

    @Test
    void whenCallcreateOperationAndOperationIdAlreadyExistReturnErrorTest() {
        NotificationsUnreachableResponse notificationsUnreachableResponse = new NotificationsUnreachableResponse();
        notificationsUnreachableResponse.setNotificationsCount(1L);

        Mockito.when(notificationService.getUnreachableNotification(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(notificationsUnreachableResponse));
        Mockito.when(operationDAO.getByOperationId(Mockito.any())).thenReturn(Mono.just(pnServiceDeskOperations));
        StepVerifier.create(service.createOperation("1234", getCreateOperationRequest()))
                .expectErrorMatches((ex) -> {
                    assertTrue(ex instanceof PnGenericException);
                    assertEquals(OPERATION_ID_IS_PRESENT, ((PnGenericException) ex).getExceptionType());
                    return true;
                }).verify();
    }

    @Test
    void searchOperationsFromRecipientInternalIdTest() {
        pnServiceDeskOperations.setOperationLastUpdateDate(Instant.now());
        Mockito.when(operationDAO.searchOperationsFromRecipientInternalId(Mockito.any())).thenReturn(Flux.just(pnServiceDeskOperations));
        assertNotNull(service.searchOperationsFromRecipientInternalId("1234", getNotificationRequest()).block());
    }

    @Test
    void whenCallpresignedUrlVideoUploadAndContentTypeIsNotValid() {
        VideoUploadRequest videoUploadRequest= getVideoUploadRequest();
        videoUploadRequest.setContentType("test");

        StepVerifier.create(service.presignedUrlVideoUpload("1234", "1234", videoUploadRequest))
                .expectErrorMatches((ex) -> {
                    assertTrue(ex instanceof PnGenericException);
                    assertEquals(ERROR_CONTENT_TYPE, ((PnGenericException) ex).getExceptionType());
                    return true;
                }).verify();
    }


    @Test
    void whenCallpresignedUrlVideoUploadAndOperationidIsNullReturnErrorTest() {
        Mockito.when(operationDAO.getByOperationId(Mockito.any())).thenReturn(Mono.empty());

        StepVerifier.create(service.presignedUrlVideoUpload("1234", "1234", getVideoUploadRequest()))
                .expectErrorMatches((ex) -> {
                    assertTrue(ex instanceof PnGenericException);
                    assertEquals(OPERATION_IS_NOT_PRESENT, ((PnGenericException) ex).getExceptionType());
                    return true;
                }).verify();
    }


    @Test
    void whenCallpresignedUrlVideoUploadAndFileLoadingReturnErrorTest() {
        Mockito.when(safeStorageClient.getFile(Mockito.any())).thenReturn(Mono.error(new PnRetryStorageException(new BigDecimal(1))));

        StepVerifier.create(service.presignedUrlVideoUpload("1234", "1234", getVideoUploadRequest()))
                .expectErrorMatches((ex) -> {
                    assertTrue(ex instanceof PnGenericException);
                    assertEquals(SAFE_STORAGE_FILE_LOADING, ((PnGenericException) ex).getExceptionType());
                    return true;
                }).verify();
    }

    @Test
    void whenCallpresignedUrlVideoUploadAndErrorDuringRecoveringFileReturnErrortest() {

        Mockito.when(safeStorageClient.getFile(Mockito.any())).thenReturn(Mono.error(new WebClientResponseException("Errore durante il recupero del file", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null)));

        StepVerifier.create(service.presignedUrlVideoUpload("1234", "1234", getVideoUploadRequest()))
                .expectErrorMatches((ex) -> {
                    assertTrue(ex instanceof PnGenericException);
                    assertEquals(ERROR_DURING_RECOVERING_FILE, ((PnGenericException) ex).getExceptionType());
                    return true;
                }).verify();
    }

    @Test
    void whenCallpresignedUrlVideoUploadAndHttpStatusIsNotFound() {

        Mockito.when(safeStorageClient.getFile(Mockito.any())).thenReturn(Mono.error(new WebClientResponseException("Errore durante il recupero del file", HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(), null, null, null)));
        Mockito.when(safeStorageClient.getPresignedUrl(Mockito.any())).thenReturn(Mono.just(fileCreationResponse));
        Mockito.when(operationsFileKeyDAO.updateVideoFileKey(Mockito.any())).thenReturn(Mono.just(pnServiceDeskOperationFileKey));

        assertNotNull(service.presignedUrlVideoUpload("1234", "1234", getVideoUploadRequest()).block());

    }

    @Test
    void whenCallpresignedUrlVideoUploadReturnVideoUploadResponseTest() {
        Mockito.when(safeStorageClient.getFile(Mockito.any())).thenReturn(Mono.just(new FileDownloadResponse()));
        Mockito.when(safeStorageClient.getPresignedUrl(Mockito.any())).thenReturn(Mono.just(fileCreationResponse));
        Mockito.when(operationsFileKeyDAO.updateVideoFileKey(Mockito.any())).thenReturn(Mono.just(pnServiceDeskOperationFileKey));

        assertNotNull(service.presignedUrlVideoUpload("1234", "1234", getVideoUploadRequest()).block());
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
        request.setContentType("application/octet-stream");
        request.setSha256("1234");
        return request;
    }

    private SearchNotificationRequest getNotificationRequest(){
        SearchNotificationRequest request = new SearchNotificationRequest();
        request.setTaxId("123");
        return request;
    }
}