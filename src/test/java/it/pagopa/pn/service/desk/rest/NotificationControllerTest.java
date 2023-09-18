package it.pagopa.pn.service.desk.rest;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationsUnreachableResponse;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.SearchNotificationRequest;
import it.pagopa.pn.service.desk.middleware.db.dao.PnClientDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnClientID;
import it.pagopa.pn.service.desk.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

@WebFluxTest(controllers = {NotificationController.class})
class NotificationControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private PnClientDAO pnClientDAO;

    @BeforeEach
    void setup() {
        Mockito.when(pnClientDAO.getByApiKey(Mockito.anyString()))
                .thenReturn(Mono.just(new PnClientID()));
    }

    @Test
    void numberOfUnreachableNotifications() {
        NotificationsUnreachableResponse response = new NotificationsUnreachableResponse();
        String path = "/service-desk/notification/unreachable";
        Mockito.when(notificationService.getUnreachableNotification(Mockito.any(), Mockito.any()))
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

    private SearchNotificationRequest getNotificationRequest(){
        SearchNotificationRequest request = new SearchNotificationRequest();
        request.setTaxId("123");
        return request;
    }
}