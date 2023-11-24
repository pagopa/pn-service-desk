package it.pagopa.pn.service.desk.rest;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.PaNotificationsRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.PaSummary;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.SearchNotificationsResponse;
import it.pagopa.pn.service.desk.middleware.db.dao.PnClientDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnClientID;
import it.pagopa.pn.service.desk.service.InfoPaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@WebFluxTest(controllers = {InfoPaController.class})
public class InfoPaControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    @MockBean
    private InfoPaService infoPaService;
    @MockBean
    private PnClientDAO pnClientDAO;

    @BeforeEach
    void setup() {
        Mockito.when(pnClientDAO.getByApiKey(Mockito.anyString()))
                .thenReturn(Mono.just(new PnClientID()));
    }

    @Test
    void getListOfOnboardedPA(){
        PaSummary response = new PaSummary();
        String path = "/service-desk/pa/activated-on-pn";
        Mockito.when(infoPaService.getListOfOnboardedPA(Mockito.any()))
                .thenReturn(Flux.just(response));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .exchange()
                .expectStatus().isOk();
    }
    
    @Test
    void searchNotificationsFromSenderId(){
        SearchNotificationsResponse response = new SearchNotificationsResponse();
        String path = "/service-desk/pa/notifications";
        Mockito.when(infoPaService.searchNotificationsFromSenderId(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(response));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .bodyValue(getPaNotificationRequest())
                .exchange()
                .expectStatus().isOk();
    }

    private PaNotificationsRequest getPaNotificationRequest() {
        PaNotificationsRequest paNotificationsRequest = new PaNotificationsRequest();
        paNotificationsRequest.setId("123");
        paNotificationsRequest.setStartDate(OffsetDateTime.parse("2023-09-29T14:02:08.203039228Z"));
        paNotificationsRequest.setEndDate(OffsetDateTime.parse("2023-10-15T14:02:08.203039228Z"));
        return paNotificationsRequest;
    }

}
