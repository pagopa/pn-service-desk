package it.pagopa.pn.service.desk.rest;

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
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

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