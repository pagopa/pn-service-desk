package it.pagopa.pn.service.desk.filter;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationsUnreachableResponse;
import it.pagopa.pn.service.desk.middleware.db.dao.PnClientDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnClientID;
import it.pagopa.pn.service.desk.service.NotificationService;
import it.pagopa.pn.service.desk.utility.Const;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import static it.pagopa.pn.service.desk.utility.Const.*;

class ClientIdWebFilterTest extends BaseTest {
    private static final String DEFAULT_URL_TEST = "/service-desk/notification/unreachable";
    private static final String PN_API_KEY = "ZEN-DESK";
    private static final String PN_API_KEY_NOT_IN_DB = "ZEN-DESK-NOT-IN-DB";
    @Autowired
    private WebTestClient webTestClient;
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private PnClientDAO pnClientDAO;

    private PnClientID entity = new PnClientID();

    private NotificationsUnreachableResponse notificationsUnreachableResponse = new NotificationsUnreachableResponse();

    @BeforeEach
    void setUp(){

        notificationsUnreachableResponse.setNotificationsCount(1L);

        Mockito.when(notificationService.getUnreachableNotification(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(new NotificationsUnreachableResponse()));

        entity.setApiKey(PN_API_KEY);
        entity.setClientId("001");
        Mockito.when(pnClientDAO.getByApiKey(PN_API_KEY))
                .thenReturn(Mono.just(entity));

        Mockito.when(pnClientDAO.getByApiKey(PN_API_KEY_NOT_IN_DB))
                .thenReturn(Mono.empty());
    }


    @Test
    void when_ClientIdHeaderNotPresent_Then_GoToService(){

        Mockito.when(notificationService.getUnreachableNotification(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(new NotificationsUnreachableResponse()));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path(DEFAULT_URL_TEST).build())
                .bodyValue(getNotificationRequest())
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void when_ClientIdHeaderPresentWithEmptyValue_Throw_UnauthorizedException(){
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path(DEFAULT_URL_TEST).build())
                .header(Const.HEADER_API_KEY, "")
                .bodyValue(getNotificationRequest())
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void when_ClientIdHeaderPresentWithValueButItsNotInDB_Throw_UnauthorizedException(){
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path(DEFAULT_URL_TEST).build())
                .header(Const.HEADER_API_KEY, PN_API_KEY_NOT_IN_DB)
                .bodyValue(getNotificationRequest())
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void when_ClientIdHeaderPresent_Then_GoTOService(){

        Mockito.when(pnClientDAO.getByApiKey(PN_API_KEY))
                .thenReturn(Mono.just(entity));
        Mockito.when(notificationService.getUnreachableNotification(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(notificationsUnreachableResponse));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path(DEFAULT_URL_TEST).build())
                .header(Const.HEADER_API_KEY, PN_API_KEY)
                .bodyValue(getNotificationRequest())
                .exchange()
                .expectStatus().isBadRequest();
    }

    private NotificationRequest getNotificationRequest(){
        NotificationRequest notificationRequest = new NotificationRequest();

        notificationRequest.setTaxId("CCARVR87N27C125H");

        return notificationRequest;
    }


}
