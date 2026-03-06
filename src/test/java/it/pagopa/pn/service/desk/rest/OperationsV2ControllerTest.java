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

import java.util.List;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.OPERATION_ID_IS_PRESENT;
import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.NO_UNREACHABLE_NOTIFICATION;
import static org.junit.jupiter.api.Assertions.assertEquals;

@WebFluxTest(controllers = {OperationsV2Controller.class})
class OperationsV2ControllerTest {

    private static final String PATH = "/service-desk/v2/act-operations";

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
    void createActOperationV2_Success() {
        CreateOperationsResponseV2 response = new CreateOperationsResponseV2();
        response.setOperationId("parentOp123");
        response.setResults(List.of());

        Mockito.when(operationsService.createActOperationV2(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(response));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path(PATH).build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .bodyValue(getCreateActOperationRequestV2())
                .exchange()
                .expectStatus().isOk()
                .expectBody(CreateOperationsResponseV2.class)
                .value(body -> assertEquals("parentOp123", body.getOperationId()));
    }

    @Test
    void createActOperationV2_DuplicateOperationId_ReturnsBadRequest() {
        Mockito.when(operationsService.createActOperationV2(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.error(new PnGenericException(OPERATION_ID_IS_PRESENT, OPERATION_ID_IS_PRESENT.getMessage())));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path(PATH).build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .bodyValue(getCreateActOperationRequestV2())
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createActOperationV2_ServiceError_ReturnsBadRequest() {
        Mockito.when(operationsService.createActOperationV2(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.error(new PnGenericException(NO_UNREACHABLE_NOTIFICATION, NO_UNREACHABLE_NOTIFICATION.getMessage())));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path(PATH).build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .bodyValue(getCreateActOperationRequestV2())
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createActOperationV2_InvalidAddressType_ReturnsBadRequest() {
        String invalidBody = "{\"iun\":[\"ABCD-EFGH-IJKL-123456-M-1\",\"ABCD-EFGH-IJKL-123456-M-2\"],"
                + "\"address\":{\"type\":\"INVALID_TYPE\"},\"taxId\":\"1234567\","
                + "\"ticketId\":\"1234\",\"ticketDate\":\"2025-07-25\",\"vrDate\":\"2025-07-25\"}";

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path(PATH).build())
                .header("x-pagopa-pn-uid", "test")
                .header("x-api-key", "test")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidBody)
                .exchange()
                .expectStatus().isBadRequest();
    }

    private CreateActOperationRequestV2 getCreateActOperationRequestV2() {
        ActDigitalAddress digitalAddress = new ActDigitalAddress();
        digitalAddress.setAddress("test@test.com");
        digitalAddress.setType(ActDigitalAddress.TypeEnum.EMAIL);

        CreateActOperationRequestV2 request = new CreateActOperationRequestV2();
        request.setIun(List.of("ABCD-EFGH-IJKL-123456-M-1", "ABCD-EFGH-IJKL-123456-M-2"));
        request.setAddress(digitalAddress);
        request.setTaxId("1234567");
        request.setTicketId("1234");
        request.setTicketOperationId("1234");
        request.setTicketDate("2025-07-25");
        request.setVrDate("2025-07-25");
        return request;
    }
}
