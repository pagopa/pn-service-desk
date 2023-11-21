package it.pagopa.pn.service.desk.rest;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.RecipientType;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.SearchNotificationsRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.SearchNotificationsResponse;
import it.pagopa.pn.service.desk.middleware.db.dao.PnClientDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnClientID;
import it.pagopa.pn.service.desk.service.NotificationAndMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

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
    void searchCourtesyMessagesFromTaxIdTest(){
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

    private SearchNotificationsRequest getSearchMessageRequest(){
        SearchNotificationsRequest searchMessagesRequest = new SearchNotificationsRequest();
        searchMessagesRequest.setTaxId("123");
        searchMessagesRequest.setRecipientType(RecipientType.PF);
        return searchMessagesRequest;
    }

}