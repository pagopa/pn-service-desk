package it.pagopa.pn.service.desk.rest;

import it.pagopa.pn.service.desk.exception.ExceptionTypeEnum;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.service.desk.middleware.db.dao.PnClientDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnClientID;
import it.pagopa.pn.service.desk.service.NotificationAndMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_ON_DELIVERY_CLIENT;
import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.TAX_ID_NOT_FOUND;

@WebFluxTest(controllers = {NotificationAndMessageController.class})
class NotificationAndMessageControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    @MockBean
    private NotificationAndMessageService notificationAndMessageService;
    @MockBean
    private PnClientDAO pnClientDAO;

    @BeforeEach
    void setup() {
        Mockito.when(pnClientDAO.getByApiKey(Mockito.anyString()))
                .thenReturn(Mono.just(new PnClientID()));
    }

    @Test
    void searchNotificationsFromTaxIdTest(){
        SearchNotificationsResponse response = new SearchNotificationsResponse();
        String path = "/service-desk/notifications";
        Mockito.when(notificationAndMessageService.searchNotificationsFromTaxId(Mockito.any(), Mockito.any(),Mockito.any(),
                Mockito.any(), Mockito.any(),Mockito.any())).thenReturn(Mono.just(response));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path(path).queryParam("size","1")
                        .queryParam("nextPagesKey","1")
                        .queryParam("startDate","2023-08-31T15:49:05.63Z")
                        .queryParam("endDate","2023-09-15T15:49:05.63Z")
                        .build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .bodyValue(getSearchMessageRequest())
                .exchange()
                .expectStatus().isOk();

    }

    @Test
    void searchNotificationsFromTaxIdKOTest(){
        String path = "/service-desk/notifications";
        Mockito.when(notificationAndMessageService.searchNotificationsFromTaxId(Mockito.any(), Mockito.any(),Mockito.any(),
                Mockito.any(), Mockito.any(),Mockito.any())).thenReturn(Mono.error(new PnGenericException(ExceptionTypeEnum.ERROR_ON_DELIVERY_CLIENT, HttpStatus.BAD_REQUEST)));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path(path).queryParam("size","1")
                        .queryParam("nextPagesKey","1")
                        .queryParam("startDate","2023-08-31T15:49:05.63Z")
                        .queryParam("endDate","2023-09-15T15:49:05.63Z")
                        .build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .bodyValue(getSearchMessageRequest())
                .exchange()
                .expectStatus().isBadRequest();

    }

    @Test
    void getTimelineOfIUNTest() {
        TimelineResponse response = new TimelineResponse();
        String path ="/service-desk/notifications/PRVZ-NZKM-JEDK-202309-A-1/timeline";
        Mockito.when(notificationAndMessageService.getTimelineOfIUN(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(response));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(path).build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .exchange()
                .expectStatus().isOk();

    }

    @Test
    void getTimelineOfIUNKOTest() {
        String path ="/service-desk/notifications/PRVZ-NZKM-JEDK-202309-A-1/timeline";
        Mockito.when(notificationAndMessageService.getTimelineOfIUN(Mockito.anyString(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.error(new PnGenericException(ExceptionTypeEnum.ERROR_ON_DELIVERY_CLIENT, HttpStatus.BAD_REQUEST)));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(path).build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .exchange()
                .expectStatus().isBadRequest();

    }

    @Test
    void getDocumentsOfIUNTest(){
        DocumentsResponse response = new DocumentsResponse();
        String path = "/service-desk/notifications/PRVZ-NZKM-JEDK-202309-A-1/documents";
        Mockito.when(notificationAndMessageService.getDocumentsOfIun(Mockito.any(), Mockito.any())).thenReturn(Mono.just(response));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path(path).build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .bodyValue(getDocumentRequest())
                .exchange()
                .expectStatus().isOk();

    }

    @Test
    void getDocumentsOfIUNKOTest(){
        String path = "/service-desk/notifications/PRVZ-NZKM-JEDK-202309-A-1/documents";
        Mockito.when(notificationAndMessageService.getDocumentsOfIun(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.error(new PnGenericException(ExceptionTypeEnum.ERROR_ON_DELIVERY_CLIENT, HttpStatus.BAD_REQUEST)));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path(path).build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .bodyValue(getDocumentRequest())
                .exchange()
                .expectStatus().isBadRequest();

    }

    @Test
    void getNotificationFromIUNTest(){
        NotificationDetailResponse response = new NotificationDetailResponse();
        String path = "/service-desk/notifications/PRVZ-NZKM-JEDK-202309-A-1";
        Mockito.when(notificationAndMessageService.getNotificationFromIUN(Mockito.any())).thenReturn(Mono.just(response));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(path).build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getNotificationFromIUNKOTest(){
        String path = "/service-desk/notifications/PRVZ-NZKM-JEDK-202309-A-1";
        Mockito.when(notificationAndMessageService.getNotificationFromIUN(Mockito.any())).thenReturn(Mono.error(new PnGenericException(ExceptionTypeEnum.ERROR_ON_DELIVERY_CLIENT, HttpStatus.BAD_REQUEST)));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(path).build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void searchNotificationsAsDelegateFromInternalIdTest() {
        SearchNotificationsResponse response = new SearchNotificationsResponse();
        String path = "/service-desk/notifications/delegate";
        Mockito.when(notificationAndMessageService.searchNotificationsAsDelegateFromInternalId(Mockito.anyString(),
                        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(response));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .queryParam("mandateId", "ajhsdfn")
                        .queryParam("delegateInternalId", "PF-asdafv4345656")
                        .queryParam("recipientType", "PF")
                        .queryParam("size","1")
                        .queryParam("nextPagesKey","1")
                        .queryParam("startDate","2023-08-31T15:49:05.63Z")
                        .queryParam("endDate","2023-09-15T15:49:05.63Z")
                        .build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void searchNotificationsAsDelegateFromInternalIdKOTest() {
        String path = "/service-desk/notifications/delegate";
        Mockito.when(notificationAndMessageService.searchNotificationsAsDelegateFromInternalId(Mockito.anyString(),
                        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.error(new PnGenericException(ExceptionTypeEnum.ERROR_ON_DELIVERY_CLIENT, HttpStatus.BAD_REQUEST)));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .queryParam("mandateId", "ajhsdfn")
                        .queryParam("delegateInternalId", "PF-asdafv4345656")
                        .queryParam("size","1")
                        .queryParam("nextPagesKey","1")
                        .queryParam("startDate","2023-08-31T15:49:05.63Z")
                        .queryParam("endDate","2023-09-15T15:49:05.63Z")
                        .build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void searchNotificationsAsDelegateFromInternalIdRecipientTypeNullTest() {
        SearchNotificationsResponse response = new SearchNotificationsResponse();
        String path = "/service-desk/notifications/delegate";
        Mockito.when(notificationAndMessageService.searchNotificationsAsDelegateFromInternalId(Mockito.anyString(),
                        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(response));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .queryParam("mandateId", "ajhsdfn")
                        .queryParam("delegateInternalId", "PF-asdafv4345656")
                        .queryParam("size","1")
                        .queryParam("nextPagesKey","1")
                        .queryParam("startDate","2023-08-31T15:49:05.63Z")
                        .queryParam("endDate","2023-09-15T15:49:05.63Z")
                        .build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void searchNotificationsAsDelegateFromInternalIdRecipientTypeWrongValueTest() {
        SearchNotificationsResponse response = new SearchNotificationsResponse();
        String path = "/service-desk/notifications/delegate";
        Mockito.when(notificationAndMessageService.searchNotificationsAsDelegateFromInternalId(Mockito.anyString(),
                        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(response));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .queryParam("mandateId", "ajhsdfn")
                        .queryParam("delegateInternalId", "PF-asdafv4345656")
                        .queryParam("recipientType", "PP")
                        .queryParam("size","1")
                        .queryParam("nextPagesKey","1")
                        .queryParam("startDate","2023-08-31T15:49:05.63Z")
                        .queryParam("endDate","2023-09-15T15:49:05.63Z")
                        .build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getNotificationRecipientDetailOkTest(){
        var response = new NotificationRecipientDetailResponse();
        var body = new NotificationRecipientDetailRequest().taxId("AAAAAAAAAAA");
        String path = "/service-desk/notifications/PRVZ-NZKM-JEDK-202309-A-1";
        Mockito.when(notificationAndMessageService.getNotificationRecipientDetail("PRVZ-NZKM-JEDK-202309-A-1", body.getTaxId())).thenReturn(Mono.just(response));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path(path).build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getNotificationRecipientDetailKoForIunTest(){
        var body = new NotificationRecipientDetailRequest().taxId("AAAAAAAAAAA");
        String path = "/service-desk/notifications/PRVZ-NZKM-JEDK-202309-A-1";
        Mockito.when(notificationAndMessageService.getNotificationRecipientDetail("PRVZ-NZKM-JEDK-202309-A-1", body.getTaxId())).thenReturn(Mono.error(new PnGenericException(ERROR_ON_DELIVERY_CLIENT,HttpStatus.NOT_FOUND)));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path(path).build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .bodyValue(body)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getNotificationRecipientDetailKoForTaxIdTest(){
        var body = new NotificationRecipientDetailRequest().taxId("AAAAAAAAAAA");
        String path = "/service-desk/notifications/PRVZ-NZKM-JEDK-202309-A-1";
        Mockito.when(notificationAndMessageService.getNotificationRecipientDetail("PRVZ-NZKM-JEDK-202309-A-1", body.getTaxId())).thenReturn(Mono.error( new PnGenericException(TAX_ID_NOT_FOUND, HttpStatus.BAD_REQUEST)));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path(path).build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .bodyValue(body)
                .exchange()
                .expectStatus().isBadRequest();
    }

    private SearchNotificationsRequest getSearchMessageRequest(){
        SearchNotificationsRequest searchMessagesRequest = new SearchNotificationsRequest();
        searchMessagesRequest.setTaxId("FRMTTR76M06B715E");
        searchMessagesRequest.setRecipientType(RecipientType.PF);
        return searchMessagesRequest;
    }

    private DocumentsRequest getDocumentRequest(){
        DocumentsRequest request = new DocumentsRequest();
        request.setTaxId("FRMTTR76M06B715E");
        request.setRecipientType(RecipientType.PF);
        return request;
    }

}
