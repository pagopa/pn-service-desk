package it.pagopa.pn.service.desk.rest;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.ResponseApiKeys;
import it.pagopa.pn.service.desk.middleware.db.dao.PnClientDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnClientID;
import it.pagopa.pn.service.desk.service.ApiKeysService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = {ApiKeysController.class})
public class ApiKeysControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    @MockBean
    private ApiKeysService apiKeysService;
    @MockBean
    private PnClientDAO pnClientDAO;

    @BeforeEach
    void setup() {
        Mockito.when(pnClientDAO.getByApiKey(Mockito.anyString()))
                .thenReturn(Mono.just(new PnClientID()));
    }

    @Test
    void getApiKeysTest (){
        ResponseApiKeys responseApiKeys = new ResponseApiKeys();
        String path = "/service-desk/api-keys";
        Mockito.when(apiKeysService.getApiKeys(Mockito.anyString())).thenReturn(Mono.just(responseApiKeys));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .queryParam("paId", "ahdk-213124-4das")
                        .build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .exchange()
                .expectStatus().isOk();
    }
}
