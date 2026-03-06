package it.pagopa.pn.service.desk.rest;

import it.pagopa.pn.service.desk.exception.ExceptionTypeEnum;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.GetOperationsResponseV2;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.OperationDetail;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.db.dao.PnClientDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnClientID;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.service.OperationsServiceV2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

@WebFluxTest(controllers = OperationsControllerV2.class)
    class OperationsControllerV2Test {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OperationsServiceV2 operationServiceV2;

    @MockBean
    private OperationDAO operationDAO;

    @MockBean
    private PnClientDAO pnClientDAO;


    @BeforeEach
    void setup() {
        Mockito.when(pnClientDAO.getByApiKey(Mockito.anyString())).thenReturn(Mono.just(new PnClientID()));
    }

    @Test
    void getOperationV2_WithIun() {
        String operationId = "op123";
        GetOperationsResponseV2 response = new GetOperationsResponseV2();
        response.setStatus("COMPLETED");
        response.setIun("IUN-12345");

        Mockito.when(operationServiceV2.getOperation(operationId)).thenReturn(Mono.just(response));

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

        Mockito.when(operationServiceV2.getOperation(operationId)).thenReturn(Mono.just(response));

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

        Mockito.when(operationServiceV2.getOperation(operationId))
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



