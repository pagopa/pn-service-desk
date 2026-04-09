package it.pagopa.pn.service.desk.rest;

import it.pagopa.pn.service.desk.exception.ExceptionTypeEnum;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.service.desk.middleware.db.dao.PnClientDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnClientID;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.service.OperationsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
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

    @Test
    void getOperationV2_WithIun() {
        String operationId = "op123";
        GetOperationsResponseV2 response = new GetOperationsResponseV2();
        response.setStatus("COMPLETED");
        response.setIun("IUN-12345");

        Mockito.when(operationsService.getOperationV2(operationId)).thenReturn(Mono.just(response));

        webTestClient.get()
                .uri("/service-desk/v2/operations/{operationId}", operationId)
                .header("x-pagopa-pn-uid", "test-uid")
                .header("x-api-key", "test-key")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(GetOperationsResponseV2.class)
                .consumeWith(result -> {
                    GetOperationsResponseV2 body = result.getResponseBody();
                    assert body != null;
                    assert "COMPLETED".equals(body.getStatus());
                    assert "IUN-12345".equals(body.getIun());
                });
    }

    @Test
    void getOperationV2_WithSubOperations() {
        String operationId = "op456";
        GetOperationsResponseV2 response = new GetOperationsResponseV2();
        response.setStatus("IN_PROGRESS");
        response.setSubOperations(List.of(new OperationDetail().iun("iun123").status("COMPLETED"),
                new OperationDetail().iun("subOp2").status("FAILED").errorReason("Error reason")));

        Mockito.when(operationsService.getOperationV2(operationId)).thenReturn(Mono.just(response));

        webTestClient.get()
                .uri("/service-desk/v2/operations/{operationId}", operationId)
                .header("x-pagopa-pn-uid", "test-uid")
                .header("x-api-key", "test-key")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(GetOperationsResponseV2.class)
                .consumeWith(result -> {
                    GetOperationsResponseV2 body = result.getResponseBody();
                    assert body != null;
                    assert "IN_PROGRESS".equals(body.getStatus());
                    assert body.getSubOperations() != null;
                    assert body.getSubOperations().size() == 2;
                    assert "COMPLETED".equals(body.getSubOperations().get(0).getStatus());
                    assert "iun123".equals(body.getSubOperations().get(0).getIun());
                    assert "FAILED".equals(body.getSubOperations().get(1).getStatus());
                    assert "subOp2".equals(body.getSubOperations().get(1).getIun());
                    assert "Error reason".equals(body.getSubOperations().get(1).getErrorReason());
                });
    }

    @Test
    void getOperationV2_isSubOperation() {
        String operationId = "op789";

        // Sub-operation scenario
        PnServiceDeskOperations subOperation = new PnServiceDeskOperations();
        subOperation.setOperationId(operationId);
        subOperation.setIsSubOperation(true);

        Mockito.when(operationsService.getOperationV2(operationId))
                .thenReturn(Mono.error(new PnGenericException(
                        ExceptionTypeEnum.OPERATION_IS_NOT_PRESENT,
                        ExceptionTypeEnum.OPERATION_IS_NOT_PRESENT.getMessage(),
                        HttpStatus.NOT_FOUND
                )));

        webTestClient.get()
                .uri("/service-desk/v2/operations/{operationId}", operationId)
                .header("x-pagopa-pn-uid", "test-uid")
                .header("x-api-key", "test-key")
                .exchange()
                .expectStatus().isNotFound();
    }
}
