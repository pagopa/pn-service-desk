package it.pagopa.pn.service.desk.rest;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.ProfileRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.ProfileResponse;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.RecipientType;
import it.pagopa.pn.service.desk.middleware.db.dao.PnClientDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnClientID;
import it.pagopa.pn.service.desk.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = {ProfileController.class})
public class ProfileControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    @MockBean
    private ProfileService profileService;
    @MockBean
    private PnClientDAO pnClientDAO;

    @BeforeEach
    void setup() {
        Mockito.when(pnClientDAO.getByApiKey(Mockito.anyString()))
                .thenReturn(Mono.just(new PnClientID()));
    }

    @Test
    void getProfileFromTaxIdTest(){
        ProfileResponse response = new ProfileResponse();
        String path = "/service-desk/profile";
        Mockito.when(profileService.getProfileFromTaxId(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(response));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .bodyValue(getProfileRequest())
                .exchange()
                .expectStatus().isOk();
    }

    private ProfileRequest getProfileRequest(){
        ProfileRequest profileRequest = new ProfileRequest();
        profileRequest.setTaxId("123");
        profileRequest.setRecipientType(RecipientType.PF);
        return profileRequest;
    }

}
