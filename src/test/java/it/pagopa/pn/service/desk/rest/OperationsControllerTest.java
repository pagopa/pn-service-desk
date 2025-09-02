package it.pagopa.pn.service.desk.rest;

import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.service.desk.middleware.db.dao.PnClientDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnClientID;
import it.pagopa.pn.service.desk.service.OperationsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_CONTENT_TYPE;
import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.NO_UNREACHABLE_NOTIFICATION;

@WebFluxTest(controllers = {OperationsController.class})
class OperationsControllerTest {

    @MockBean
    private OperationsService operationsService;

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private PnClientDAO pnClientDAO;

    @BeforeEach
    void setup() {
        Mockito.when(pnClientDAO.getByApiKey(Mockito.anyString()))
                .thenReturn(Mono.just(new PnClientID()));
    }

    @Test
    void createOperation() {
        OperationsResponse response = new OperationsResponse();
        String path = "/service-desk/operations";
        Mockito.when(operationsService.createOperation(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(response));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .bodyValue(getCreateOperationRequest())
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void createOperationKO() {
        String path = "/service-desk/operations";
        Mockito.when(operationsService.createOperation(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.error(new PnGenericException(NO_UNREACHABLE_NOTIFICATION,NO_UNREACHABLE_NOTIFICATION.getMessage())));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .bodyValue(getCreateOperationRequest())
                .exchange()
                .expectStatus().isBadRequest();
    }


    @Test
    void createActOperation() {
        OperationsResponse response = new OperationsResponse();
        String path = "/service-desk/act-operations";
        Mockito.when(operationsService.createActOperation(Mockito.any(), Mockito.any()))
               .thenReturn(Mono.just(response));

        webTestClient.post()
                     .uri(uriBuilder -> uriBuilder.path(path)
                                                  .build())
                     .header("x-pagopa-pn-uid", "test")
                     .header("x-api-key", "test")
                     .bodyValue(getCreateActOperationRequest())
                     .exchange()
                     .expectStatus().isCreated();
    }

    @Test
    void createActOperationKo() {
        String path = "/service-desk/act-operations";

        // Simuliamo che il service ritorni un errore
        Mockito.when(operationsService.createActOperation(Mockito.any(), Mockito.any()))
               .thenReturn(Mono.error(new PnGenericException(NO_UNREACHABLE_NOTIFICATION, NO_UNREACHABLE_NOTIFICATION.getMessage())));

        webTestClient.post()
                     .uri(uriBuilder -> uriBuilder.path(path).build())
                     .header("x-pagopa-pn-uid", "test")
                     .header("x-api-key", "test")
                     .bodyValue(getCreateActOperationRequest())
                     .exchange()
                     .expectStatus().isBadRequest();  // Aspettiamo un 400 Bad Request o altro status previsto
    }

    @Test
    void createActOperation_WrongAddressType_Ko() {
        String path = "/service-desk/act-operations";
        String requestBody = "{\"address\":{\"type\":\"INVALID_TYPE\"}}";

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path(path).build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }


    @Test
    void searchOperationsFromTaxId() {
        SearchResponse response = new SearchResponse();
        String path = "/service-desk/operations/search";
        Mockito.when(operationsService.searchOperationsFromRecipientInternalId(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(response));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .bodyValue(getNotificationRequest())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void presignedUrlVideoUpload() {
        VideoUploadResponse response = new VideoUploadResponse();
        String path = "/service-desk/1234/video-upload";
        Mockito.when(operationsService.presignedUrlVideoUpload(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(response));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .bodyValue(getVideoUploadRequest())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void presignedUrlVideoUploadKO() {
        String path = "/service-desk/1234/video-upload";
        Mockito.when(operationsService.presignedUrlVideoUpload(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.error(new PnGenericException(ERROR_CONTENT_TYPE, ERROR_CONTENT_TYPE.getMessage())));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .bodyValue(getVideoUploadRequest())
                .exchange()
                .expectStatus().isBadRequest();
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


    private CreateActOperationRequest getCreateActOperationRequest(){
        CreateActOperationRequest request = new CreateActOperationRequest();
        ActDigitalAddress digitalAddress= new ActDigitalAddress();
        digitalAddress.setAddress("test@test.com");
        digitalAddress.setType(ActDigitalAddress.TypeEnum.EMAIL);

        request.setIun("ABCD-EFGH-IJKL-123456-M-1");
        request.setTicketDate("2025-07-25");
        request.setVrDate("2025-07-25");
        request.setTaxId("1234567");
        request.setAddress(digitalAddress);
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


    @Test
    void getOperationStatus_Success() {
        String operationId = "op123";
        String expectedStatus = "OK";

        Mockito.when(operationsService.getOperationStatus(operationId))
                .thenReturn(Mono.just(expectedStatus));

        webTestClient.get()
                     .uri("/service-desk/operation/{operationId}", operationId)
                     .header("x-pagopa-pn-uid", "test")
                     .header("x-api-key", "test")
                     .exchange()
                     .expectStatus().isOk()
                     .expectBody(String.class)
                     .isEqualTo(expectedStatus);
    }

    @Test
    void getOperationStatus_NotFound() {
        String operationId = "op404";

        Mockito.when(operationsService.getOperationStatus(operationId))
                .thenReturn(Mono.error(new RuntimeException("Operation not found")));

        webTestClient.get()
                     .uri("/service-desk/operation/{operationId}", operationId)
                     .header("x-pagopa-pn-uid", "test")
                     .header("x-api-key", "test")
                     .exchange()
                     .expectStatus().is5xxServerError()
                     .expectBody(String.class)
                     .consumeWith(result ->
         new String(result.getResponseBody()).contains("Operation not found"));
    }
}