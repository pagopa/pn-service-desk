package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.exception.PnRetryStorageException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationRecipientV24Dto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationV25Dto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileCreationResponse;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationsFileKeyDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperationFileKey;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault.PnDataVaultClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.safestorage.PnSafeStorageClient;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import it.pagopa.pn.service.desk.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.*;
import static org.junit.jupiter.api.Assertions.*;

class OperationsServiceImplTest extends BaseTest {
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private PnDeliveryClient pnDeliveryClient;
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

    private CreateActOperationRequest createActOperationRequest;


    private final PnServiceDeskOperations pnServiceDeskOperations =new PnServiceDeskOperations();
    private final PnServiceDeskOperations operationV2 =new PnServiceDeskOperations();
    private final PnServiceDeskOperationFileKey pnServiceDeskOperationFileKey= new PnServiceDeskOperationFileKey();
    private final FileCreationResponse fileCreationResponse=new FileCreationResponse();


    @BeforeEach
    void inizialize(){

        createActOperationRequest = new CreateActOperationRequest();
        createActOperationRequest.setTaxId("AAAAAA00A00A000A");
        createActOperationRequest.setIun("iun123");
        createActOperationRequest.setTicketId("ticket123");
        createActOperationRequest.setTicketOperationId("op123");
        createActOperationRequest.setAddress(new ActDigitalAddress().address("test@test.com").type(ActDigitalAddress.TypeEnum.EMAIL));


        pnServiceDeskOperations.setOperationId("123");
        pnServiceDeskOperations.setOperationStartDate(Instant.now());
        pnServiceDeskOperations.setOperationLastUpdateDate(Instant.now());
        pnServiceDeskOperations.setStatus("CREATING");
        pnServiceDeskOperations.setRecipientInternalId("1234");

        pnServiceDeskOperationFileKey.setOperationId("1234");
        pnServiceDeskOperationFileKey.setFileKey("1234");

        fileCreationResponse.setKey("123");
        fileCreationResponse.setUploadUrl("test");
        fileCreationResponse.setSecret("secret");

        operationV2.setOperationId("op123");
        operationV2.setStatus("WARNING");
        operationV2.setErrorReason(null);
        operationV2.setIsSubOperation(false);

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
                    assertInstanceOf(PnGenericException.class, ex);
                    assertEquals(OPERATION_ID_IS_PRESENT, ((PnGenericException) ex).getExceptionType());
                    return true;
                }).verify();
    }

    @Test
    void searchOperationsFromRecipientInternalIdTest() {
        pnServiceDeskOperations.setOperationLastUpdateDate(Instant.now());
        Mockito.when(operationDAO.searchOperationsFromRecipientInternalId(Mockito.any())).thenReturn(Flux.just(pnServiceDeskOperations));
        assertNotNull(service.searchOperationsFromRecipientInternalId("1234", getNotificationRequest()));
    }

    @Test
    void whenCallpresignedUrlVideoUploadAndContentTypeIsNotValid() {
        VideoUploadRequest videoUploadRequest= getVideoUploadRequest();
        videoUploadRequest.setContentType("test");

        StepVerifier.create(service.presignedUrlVideoUpload("1234", "1234", videoUploadRequest))
                .expectErrorMatches((ex) -> {
                    assertInstanceOf(PnGenericException.class, ex);
                    assertEquals(ERROR_CONTENT_TYPE, ((PnGenericException) ex).getExceptionType());
                    return true;
                }).verify();
    }


    @Test
    void whenCallpresignedUrlVideoUploadAndOperationidIsNullReturnErrorTest() {
        Mockito.when(operationDAO.getByOperationId(Mockito.any())).thenReturn(Mono.empty());

        StepVerifier.create(service.presignedUrlVideoUpload("1234", "1234", getVideoUploadRequest()))
                .expectErrorMatches((ex) -> {
                    assertInstanceOf(PnGenericException.class, ex);
                    assertEquals(OPERATION_IS_NOT_PRESENT, ((PnGenericException) ex).getExceptionType());
                    return true;
                }).verify();
    }


    @Test
    void whenCallpresignedUrlVideoUploadAndFileLoadingReturnErrorTest() {
        Mockito.when(safeStorageClient.getFile(Mockito.any())).thenReturn(Mono.error(new PnRetryStorageException(new BigDecimal(1))));

        StepVerifier.create(service.presignedUrlVideoUpload("1234", "1234", getVideoUploadRequest()))
                .expectErrorMatches((ex) -> {
                    assertInstanceOf(PnGenericException.class, ex);
                    assertEquals(SAFE_STORAGE_FILE_LOADING, ((PnGenericException) ex).getExceptionType());
                    return true;
                }).verify();
    }

    @Test
    void whenCallpresignedUrlVideoUploadAndErrorDuringRecoveringFileReturnErrortest() {

        Mockito.when(safeStorageClient.getFile(Mockito.any())).thenReturn(Mono.error(new WebClientResponseException("Errore durante il recupero del file", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null)));

        StepVerifier.create(service.presignedUrlVideoUpload("1234", "1234", getVideoUploadRequest()))
                .expectErrorMatches((ex) -> {
                    assertInstanceOf(PnGenericException.class, ex);
                    assertEquals(ERROR_DURING_RECOVERING_FILE, ((PnGenericException) ex).getExceptionType());
                    return true;
                }).verify();
    }

    @Test
    void whenCallpresignedUrlVideoUploadAndHttpStatusIsNotFound() {

        Mockito.when(safeStorageClient.getFile(Mockito.any())).thenReturn(Mono.error(new WebClientResponseException("Errore durante il recupero del file", HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(), null, null, null)));
        Mockito.when(safeStorageClient.getPresignedUrl(Mockito.any())).thenReturn(Mono.just(fileCreationResponse));
        Mockito.when(operationsFileKeyDAO.updateVideoFileKey(Mockito.any())).thenReturn(Mono.just(pnServiceDeskOperationFileKey));
        Mockito.when(operationDAO.updateEntity(Mockito.any())).thenReturn(Mono.just(pnServiceDeskOperations));

        assertNotNull(service.presignedUrlVideoUpload("1234", "1234", getVideoUploadRequest()).block());

    }

    @Test
    void whenCallpresignedUrlVideoUploadReturnVideoUploadResponseTest() {
        pnServiceDeskOperations.setStatus(NotificationStatus.StatusEnum.CREATING.getValue());
        Mockito.when(operationDAO.getByOperationId(Mockito.any())).thenReturn(Mono.just(pnServiceDeskOperations));
        Mockito.when(safeStorageClient.getFile(Mockito.any())).thenReturn(Mono.just(new FileDownloadResponse()));
        Mockito.when(safeStorageClient.getPresignedUrl(Mockito.any())).thenReturn(Mono.just(fileCreationResponse));
        Mockito.when(operationsFileKeyDAO.updateVideoFileKey(Mockito.any())).thenReturn(Mono.just(pnServiceDeskOperationFileKey));
        Mockito.when(operationDAO.updateEntity(Mockito.any())).thenReturn(Mono.just(pnServiceDeskOperations));

        assertNotNull(service.presignedUrlVideoUpload("1234", "1234", getVideoUploadRequest()).block());
    }

    @Test
    void whenCallPresignedUrlVideoUploadAndOperationNotInCreatingStatusReturn409ConflictTest() {
        // Setup operation with status different from CREATING
        pnServiceDeskOperations.setStatus(NotificationStatus.StatusEnum.VALIDATION.getValue());
        Mockito.when(operationDAO.getByOperationId(Mockito.any())).thenReturn(Mono.just(pnServiceDeskOperations));

        StepVerifier.create(service.presignedUrlVideoUpload("1234", "1234", getVideoUploadRequest()))
                .expectErrorMatches((ex) -> {
                    assertInstanceOf(PnGenericException.class, ex);
                    assertEquals(FILE_ALREADY_UPLOADED, ((PnGenericException) ex).getExceptionType());
                    assertEquals(HttpStatus.CONFLICT, ((PnGenericException) ex).getHttpStatus());
                    return true;
                }).verify();
    }

    @Test
    void whenCallPresignedUrlVideoUploadSuccessfullyThenStatusUpdatedToValidationTest() {
        // Setup operation with CREATING status
        pnServiceDeskOperations.setStatus(NotificationStatus.StatusEnum.CREATING.getValue());
        Mockito.when(operationDAO.getByOperationId(Mockito.any())).thenReturn(Mono.just(pnServiceDeskOperations));
        Mockito.when(safeStorageClient.getFile(Mockito.any())).thenReturn(Mono.error(new WebClientResponseException("File not found", HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(), null, null, null)));
        Mockito.when(safeStorageClient.getPresignedUrl(Mockito.any())).thenReturn(Mono.just(fileCreationResponse));
        Mockito.when(operationsFileKeyDAO.updateVideoFileKey(Mockito.any())).thenReturn(Mono.just(pnServiceDeskOperationFileKey));
        Mockito.when(operationDAO.updateEntity(Mockito.any())).thenReturn(Mono.just(pnServiceDeskOperations));

        VideoUploadResponse result = service.presignedUrlVideoUpload("1234", "1234", getVideoUploadRequest()).block();
        
        assertNotNull(result);
        // Verify that updateEntity was called with operation status set to VALIDATION
        Mockito.verify(operationDAO).updateEntity(Mockito.argThat(operation -> 
                OperationStatusEnum.VALIDATION.toString().equals(operation.getStatus())));
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


    @Test
    void createActOperation_Success() {
        SentNotificationV25Dto sentNotificationV25Dto = new SentNotificationV25Dto();

        NotificationRecipientV24Dto recipient = new NotificationRecipientV24Dto();
        recipient.setTaxId(createActOperationRequest.getTaxId());
        sentNotificationV25Dto.setRecipients(List.of(recipient));
        sentNotificationV25Dto.setIun(createActOperationRequest.getIun());
        sentNotificationV25Dto.setDocumentsAvailable(true);


        Mockito.when(pnDeliveryClient.getSentNotificationPrivate(
                Mockito.eq(createActOperationRequest.getIun())))
               .thenReturn(Mono.just(sentNotificationV25Dto));

        Mockito.when(operationDAO.getByOperationId(Mockito.any()))
               .thenReturn(Mono.empty());

        Mockito.when(operationDAO.createOperationAndAddress(Mockito.any(), Mockito.any()))
               .thenReturn(Mono.just(Tuples.of(pnServiceDeskOperations, new PnServiceDeskAddress())));

        StepVerifier.create(service.createActOperation("someUid", createActOperationRequest))
                    .assertNext(response -> {
                        assertNotNull(response);
                        assertNotNull(response.getOperationId());
                    })
                    .verifyComplete();
    }



    @Test
    void createActOperation_NoLegalFacts_CompletesWithoutError() {
        Mockito.when(pnDeliveryClient.getSentNotificationPrivate(
                       createActOperationRequest.getIun()))
               .thenReturn(Mono.empty());

        StepVerifier.create(service.createActOperation("someUid", createActOperationRequest))
                    .verifyComplete();    }


    @Test
    void createActOperation_TaxIdMismatch_ReturnsError() {
        SentNotificationV25Dto sentNotification = new SentNotificationV25Dto();

        NotificationRecipientV24Dto recipient = new NotificationRecipientV24Dto();
        // Simuliamo mismatch
        recipient.setTaxId("DIFFERENT_TAX_ID");

        sentNotification.setRecipients(List.of(recipient));
        sentNotification.setDocumentsAvailable(true);

        Mockito.when(pnDeliveryClient.getSentNotificationPrivate(
                       createActOperationRequest.getIun()))
               .thenReturn(Mono.just(sentNotification));

        StepVerifier.create(service.createActOperation("someUid", createActOperationRequest))
                    .expectErrorSatisfies(ex -> {
                        assertInstanceOf(PnGenericException.class, ex);
                        assertTrue(ex.getMessage().contains("Tax ID from request does not match"));
                    })
                    .verify();
    }


    @Test
    void createActOperation_ClientError_PropagatesPnGenericException() {
        Mockito.when(pnDeliveryClient.getSentNotificationPrivate(
                       createActOperationRequest.getIun()))
               .thenReturn(Mono.error(new RuntimeException("Simulated client error")));

        StepVerifier.create(service.createActOperation("someUid", createActOperationRequest))
                    .expectErrorSatisfies(ex -> {
                        assertInstanceOf(PnGenericException.class, ex);
                        assertTrue(ex.getMessage().contains("Simulated client error"));
                    })
                    .verify();
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

    @Test
    void createActOperationV2_AllValid_CreatesParentAndSubOps() {
        SentNotificationV25Dto sentNotificationV25Dto = new SentNotificationV25Dto();
        NotificationRecipientV24Dto recipient = new NotificationRecipientV24Dto();
        recipient.setTaxId("AAAAAA00A00A000A");
        sentNotificationV25Dto.setRecipients(List.of(recipient));
        sentNotificationV25Dto.setDocumentsAvailable(true);

        Mockito.when(operationDAO.getByOperationId(Mockito.any())).thenReturn(Mono.empty());
        Mockito.when(pnDeliveryClient.getSentNotificationPrivate(Mockito.any()))
               .thenReturn(Mono.just(sentNotificationV25Dto));
        Mockito.when(operationDAO.createParentOperationWithSubOpsAndAddress(Mockito.any(), Mockito.any(), Mockito.any()))
               .thenReturn(Mono.just(pnServiceDeskOperations));

        CreateActOperationRequestV2 request = getCreateActOperationRequestV2();

        StepVerifier.create(service.createActOperationV2("uid", request))
                    .assertNext(response -> {
                        assertNotNull(response);
                        assertNotNull(response.getOperationId());
                        assertNotNull(response.getResults());
                        assertEquals(2, response.getResults().size());
                        response.getResults().forEach(r -> assertEquals("CREATING", r.getStatus()));
                    })
                    .verifyComplete();
    }

    @Test
    void createActOperationV2_DuplicateIun_Returns400() {
        CreateActOperationRequestV2 request = getCreateActOperationRequestV2();
        request.setIun(List.of("IUN1", "IUN2", "IUN1"));

        StepVerifier.create(service.createActOperationV2("uid", request))
                    .expectErrorMatches(ex -> {
                        assertInstanceOf(PnGenericException.class, ex);
                        assertEquals(DUPLICATE_IUN_IN_REQUEST, ((PnGenericException) ex).getExceptionType());
                        assertEquals(HttpStatus.BAD_REQUEST, ((PnGenericException) ex).getHttpStatus());
                        return true;
                    })
                    .verify();

        Mockito.verify(operationDAO, Mockito.never())
               .createParentOperationWithSubOpsAndAddress(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void createActOperationV2_DuplicateOperationId_Returns400() {
        Mockito.when(operationDAO.getByOperationId(Mockito.any())).thenReturn(Mono.just(pnServiceDeskOperations));

        StepVerifier.create(service.createActOperationV2("uid", getCreateActOperationRequestV2()))
                    .expectErrorMatches(ex -> {
                        assertInstanceOf(PnGenericException.class, ex);
                        assertEquals(OPERATION_ID_IS_PRESENT, ((PnGenericException) ex).getExceptionType());
                        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, ((PnGenericException) ex).getHttpStatus());
                        return true;
                    })
                    .verify();

        Mockito.verify(operationDAO, Mockito.never())
               .createParentOperationWithSubOpsAndAddress(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void createActOperationV2_PartialValid_CreatesWithValidSubOpsOnly() {
        SentNotificationV25Dto validNotification = new SentNotificationV25Dto();
        NotificationRecipientV24Dto recipient = new NotificationRecipientV24Dto();
        recipient.setTaxId("AAAAAA00A00A000A");
        validNotification.setRecipients(List.of(recipient));
        validNotification.setDocumentsAvailable(true);

        SentNotificationV25Dto mismatchNotification = new SentNotificationV25Dto();
        NotificationRecipientV24Dto mismatchRecipient = new NotificationRecipientV24Dto();
        mismatchRecipient.setTaxId("DIFFERENT_TAX_ID");
        mismatchNotification.setRecipients(List.of(mismatchRecipient));
        mismatchNotification.setDocumentsAvailable(true);

        Mockito.when(operationDAO.getByOperationId(Mockito.any())).thenReturn(Mono.empty());
        Mockito.when(pnDeliveryClient.getSentNotificationPrivate("IUN1"))
               .thenReturn(Mono.just(validNotification));
        Mockito.when(pnDeliveryClient.getSentNotificationPrivate("IUN2"))
               .thenReturn(Mono.just(mismatchNotification));
        Mockito.when(operationDAO.createParentOperationWithSubOpsAndAddress(Mockito.any(), Mockito.any(), Mockito.any()))
               .thenReturn(Mono.just(pnServiceDeskOperations));

        CreateActOperationRequestV2 request = getCreateActOperationRequestV2();

        StepVerifier.create(service.createActOperationV2("uid", request))
                    .assertNext(response -> {
                        assertNotNull(response);
                        assertNotNull(response.getResults());
                        assertEquals(2, response.getResults().size());
                        long okCount = response.getResults().stream().filter(r -> "CREATING".equals(r.getStatus())).count();
                        long koCount = response.getResults().stream().filter(r -> "KO".equals(r.getStatus())).count();
                        assertEquals(1, okCount);
                        assertEquals(1, koCount);
                    })
                    .verifyComplete();
    }

    @Test
    void createActOperationV2_AllFail_CreatesParentWithKoStatus() {
        SentNotificationV25Dto mismatchNotification = new SentNotificationV25Dto();
        NotificationRecipientV24Dto mismatchRecipient = new NotificationRecipientV24Dto();
        mismatchRecipient.setTaxId("DIFFERENT_TAX_ID");
        mismatchNotification.setRecipients(List.of(mismatchRecipient));
        mismatchNotification.setDocumentsAvailable(true);

        Mockito.when(operationDAO.getByOperationId(Mockito.any())).thenReturn(Mono.empty());
        Mockito.when(pnDeliveryClient.getSentNotificationPrivate(Mockito.any()))
               .thenReturn(Mono.just(mismatchNotification));
        Mockito.when(operationDAO.createOperation(Mockito.any()))
               .thenReturn(Mono.just(pnServiceDeskOperations));

        CreateActOperationRequestV2 request = getCreateActOperationRequestV2();

        StepVerifier.create(service.createActOperationV2("uid", request))
                    .assertNext(response -> {
                        assertNotNull(response);
                        assertNotNull(response.getResults());
                        assertEquals(2, response.getResults().size());
                        response.getResults().forEach(r -> assertEquals("KO", r.getStatus()));
                    })
                    .verifyComplete();

        Mockito.verify(operationDAO).createOperation(
                Mockito.argThat(parent -> OperationStatusEnum.KO.toString().equals(parent.getStatus())
                        && parent.getSubOperationsIds().isEmpty())
        );
        Mockito.verify(operationDAO, Mockito.never())
               .createParentOperationWithSubOpsAndAddress(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void createActOperationV2_DeliveryClientError_MarksIunAsKoAndCreatesParent() {
        Mockito.when(operationDAO.getByOperationId(Mockito.any())).thenReturn(Mono.empty());
        Mockito.when(pnDeliveryClient.getSentNotificationPrivate(Mockito.any()))
               .thenReturn(Mono.error(new RuntimeException("Delivery service unavailable")));
        Mockito.when(operationDAO.createOperation(Mockito.any()))
               .thenReturn(Mono.just(pnServiceDeskOperations));

        CreateActOperationRequestV2 request = getCreateActOperationRequestV2();

        StepVerifier.create(service.createActOperationV2("uid", request))
                    .assertNext(response -> {
                        assertNotNull(response);
                        assertNotNull(response.getResults());
                        assertEquals(2, response.getResults().size());
                        response.getResults().forEach(r -> {
                            assertEquals("KO", r.getStatus());
                            assertNotNull(r.getErrorReason());
                        });
                    })
                    .verifyComplete();

        Mockito.verify(operationDAO).createOperation(
                Mockito.argThat(parent -> OperationStatusEnum.KO.toString().equals(parent.getStatus())
                        && parent.getSubOperationsIds().isEmpty())
        );
        Mockito.verify(operationDAO, Mockito.never())
               .createParentOperationWithSubOpsAndAddress(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void getOperation_V1Operation_ReturnsResponse() {

        operationV2.setIun("iun123");

        Mockito.when(operationDAO.getByOperationId("op123"))
                .thenReturn(Mono.just(operationV2));

        StepVerifier.create(service.getOperationV2("op123"))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals("WARNING", response.getStatus());
                    assertEquals("iun123", response.getIun());
                })
                .verifyComplete();
    }

    @Test
    void getOperation_WithSubOperations_ReturnsSubOperations() {

        operationV2.setSubOperationsIds(List.of("sub1", "sub2"));

        PnServiceDeskOperations sub1 = new PnServiceDeskOperations();
        sub1.setStatus("OK");
        sub1.setIun("iun1");
        sub1.setOperationId("sub1");

        PnServiceDeskOperations sub2 = new PnServiceDeskOperations();
        sub2.setStatus("KO");
        sub2.setErrorReason("ERROR");
        sub2.setIun("iun2");
        sub2.setOperationId("sub2");

        Mockito.when(operationDAO.getByOperationId("op123"))
                .thenReturn(Mono.just(operationV2));

        Mockito.when(operationDAO.getByOperationId("sub1"))
                .thenReturn(Mono.just(sub1));

        Mockito.when(operationDAO.getByOperationId("sub2"))
                .thenReturn(Mono.just(sub2));

        StepVerifier.create(service.getOperationV2("op123"))
                .assertNext(response -> {
                    assertEquals(2, response.getSubOperations().size());

                    OperationDetail first = response.getSubOperations().get(0);
                    assertEquals("OK", first.getStatus());
                })
                .verifyComplete();
    }

    @Test
    void getOperation_NotFound_Returns404() {

        Mockito.when(operationDAO.getByOperationId("op123"))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.getOperationV2("op123"))
                .expectErrorMatches(ex -> {
                    assertTrue(ex instanceof PnGenericException);
                    assertEquals(OPERATION_IS_NOT_PRESENT,
                            ((PnGenericException) ex).getExceptionType());
                    assertEquals(HttpStatus.NOT_FOUND,
                            ((PnGenericException) ex).getHttpStatus());
                    return true;
                })
                .verify();
    }

    @Test
    void getOperation_IsSubOperation_Returns404() {

        operationV2.setIsSubOperation(true);

        Mockito.when(operationDAO.getByOperationId("op123"))
                .thenReturn(Mono.just(operationV2));

        StepVerifier.create(service.getOperationV2("op123"))
                .expectError(PnGenericException.class)
                .verify();
    }

    @Test
    void getOperation_WebClientException_ReturnsBadRequest() {

        Mockito.when(operationDAO.getByOperationId("op123"))
                .thenReturn(Mono.error(
                        new WebClientResponseException(
                                "Error",
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                null,
                                null,
                                null)));

        StepVerifier.create(service.getOperationV2("op123"))
                .expectErrorMatches(ex -> {
                    assertTrue(ex instanceof PnGenericException);
                    assertEquals(ERROR_DURING_GET_OPERATION_V2,
                            ((PnGenericException) ex).getExceptionType());
                    assertEquals(HttpStatus.BAD_REQUEST,
                            ((PnGenericException) ex).getHttpStatus());
                    return true;
                })
                .verify();
    }

    private CreateActOperationRequestV2 getCreateActOperationRequestV2() {
        CreateActOperationRequestV2 request = new CreateActOperationRequestV2();
        request.setTaxId("AAAAAA00A00A000A");
        request.setIun(List.of("IUN1", "IUN2"));
        request.setTicketId("ticket123");
        request.setTicketOperationId("op123");
        request.setTicketDate("2024-01-01");
        request.setVrDate("2024-01-02");
        request.setAddress(new ActDigitalAddress().address("test@test.com").type(ActDigitalAddress.TypeEnum.EMAIL));
        return request;
    }

}