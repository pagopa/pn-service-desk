package it.pagopa.pn.service.desk.rest;

import it.pagopa.pn.service.desk.exception.ExceptionTypeEnum;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.service.desk.middleware.db.dao.PnClientDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnClientID;
import it.pagopa.pn.service.desk.service.InfoPaService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@WebFluxTest(controllers = {InfoPaController.class})
class InfoPaControllerTest {

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
    void getListOfOnboardedPATest(){
        PaSummary response = new PaSummary();
        String path = "/service-desk/pa/activated-on-pn";
        Mockito.when(infoPaService.getListOfOnboardedPA(Mockito.any(), Mockito.isNull()))
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
    void getListOfOnboardedPAWithPaNameFilterTest(){
        PaSummary response = new PaSummary();
        String path = "/service-desk/pa/activated-on-pn";
        String paNameFilter = "Pal";
        Mockito.when(infoPaService.getListOfOnboardedPA(Mockito.any(), Mockito.eq(paNameFilter)))
                .thenReturn(Flux.just(response));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .queryParam("paNameFilter", paNameFilter)
                        .build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getListOfOnboardedPAKOTest(){
        String path = "/service-desk/pa/activated-on-pn";
        Mockito.when(infoPaService.getListOfOnboardedPA(Mockito.any(), Mockito.isNull()))
                .thenReturn(Flux.error(new PnGenericException(ExceptionTypeEnum.ERROR_ON_EXTERNAL_REGISTRIES_CLIENT, HttpStatus.BAD_REQUEST)));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getExtendedListOfOnboardedPA(){
        // Given
        PaSummaryExtendedResponse paSummaryExtendedResponse = new PaSummaryExtendedResponse();
        String path = "/service-desk/pa/v2/activated-on-pn";

        // When
        Mockito.when(infoPaService.getExtendedListOfOnboardedPA(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(paSummaryExtendedResponse));

        // Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void getExtendedListOfOnboardedPA_PaNameFilter(){
        // Given
        String path = "/service-desk/pa/v2/activated-on-pn";
        String paNameFilter = "Comune";
        Integer page = 1;
        Integer size = 10;
        boolean onlyChildren = false;

        List<PaSummaryExtended> paSummaryExtendedList = new ArrayList<>();
        PaSummaryExtended paSummaryExtended = new PaSummaryExtended();
        paSummaryExtended.setId("0");
        paSummaryExtended.setName("Comune di Firenze");
        paSummaryExtendedList.add(paSummaryExtended);
        PaSummaryExtendedResponse paSummaryExtendedResponse = new PaSummaryExtendedResponse();
        paSummaryExtendedResponse.setContent(paSummaryExtendedList);

        // When
        Mockito.when(infoPaService.getExtendedListOfOnboardedPA(Mockito.any(), Mockito.eq(paNameFilter), Mockito.eq(onlyChildren), Mockito.eq(page), Mockito.eq(size)))
                .thenReturn(Mono.just(paSummaryExtendedResponse));

        // Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .queryParam("paNameFilter", paNameFilter)
                        .queryParam("onlyChildren", onlyChildren)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PaSummaryExtendedResponse.class)
                .hasSize(1)
                .contains(paSummaryExtendedResponse);
    }

    @Test
    void getExtendedListOfOnboardedPA_OnlyChildren() {
        // Given
        String path = "/service-desk/pa/v2/activated-on-pn";
        String paNameFilter = "Comune";
        Integer page = 1;
        Integer size = 10;
        Boolean onlyChildren = true;

        List<PaSummaryExtended> responseList = new ArrayList<>();
        PaSummaryExtended father = new PaSummaryExtended();
        father.setId("987654321");
        father.setName("Consiglio Regionale del Lazio");

        List<PaSummaryExtendedInfo> childrenList = new ArrayList<>();
        PaSummaryExtendedInfo child = new PaSummaryExtendedInfo();
        child.setId("18346279");
        child.setName("Ufficio tributi Roma");
        childrenList.add(child);
        father.setChildrenList(childrenList);
        responseList.add(father);

        PaSummaryExtendedResponse paSummaryExtendedResponse = new PaSummaryExtendedResponse();
        paSummaryExtendedResponse.setContent(responseList);

        // When
        Mockito.when(infoPaService.getExtendedListOfOnboardedPA(Mockito.any(), Mockito.any(), Mockito.eq(onlyChildren), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(paSummaryExtendedResponse));

        // Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .queryParam("onlyChildren", onlyChildren)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(PaSummaryExtendedResponse.class)
                .value(response -> {
                    Assertions.assertNotNull(response);
                    Assertions.assertEquals(1, response.getContent().size());
                    Assertions.assertEquals("987654321", response.getContent().get(0).getId());
                    Assertions.assertEquals("Consiglio Regionale del Lazio", response.getContent().get(0).getName());
                    Assertions.assertEquals("18346279", response.getContent().get(0).getChildrenList().get(0).getId());
                    Assertions.assertEquals("Ufficio tributi Roma", response.getContent().get(0).getChildrenList().get(0).getName());
                });
    }

    @Test
    void getExtendedListOfOnboardedPA_KO(){
        // Given
        String path = "/service-desk/pa/v2/activated-on-pn";

        // When
        Mockito.when(infoPaService.getExtendedListOfOnboardedPA(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.error(new PnGenericException(ExceptionTypeEnum.ERROR_ON_EXTERNAL_REGISTRIES_CLIENT, HttpStatus.BAD_REQUEST)));

        // Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .exchange()
                .expectStatus()
                .isBadRequest();
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

    @Test
    void searchNotificationsFromSenderIdKO(){
        String path = "/service-desk/pa/notifications";
        Mockito.when(infoPaService.searchNotificationsFromSenderId(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.error(new PnGenericException(ExceptionTypeEnum.ERROR_ON_DELIVERY_CLIENT, HttpStatus.BAD_REQUEST)));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .bodyValue(getPaNotificationRequest())
                .exchange()
                .expectStatus().isBadRequest();
    }

    private PaNotificationsRequest getPaNotificationRequest() {
        PaNotificationsRequest paNotificationsRequest = new PaNotificationsRequest();
        paNotificationsRequest.setId("123");
        paNotificationsRequest.setStartDate(Instant.parse("2023-09-29T14:02:08.203039228Z"));
        paNotificationsRequest.setEndDate(Instant.parse("2023-10-15T14:02:08.203039228Z"));
        return paNotificationsRequest;
    }

}
