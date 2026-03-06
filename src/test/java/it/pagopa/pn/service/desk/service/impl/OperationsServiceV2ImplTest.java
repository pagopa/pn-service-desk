package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.OperationDetail;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.service.OperationsServiceV2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.*;
import static org.junit.jupiter.api.Assertions.*;

class OperationsServiceV2ImplTest extends BaseTest {

    @MockBean
    private OperationDAO operationDAO;

    @Autowired
    private OperationsServiceV2 service;

    private PnServiceDeskOperations operation;

    @BeforeEach
    void init() {
        operation = new PnServiceDeskOperations();
        operation.setOperationId("op123");
        operation.setStatus("WARNING");
        operation.setErrorReason(null);
        operation.setIsSubOperation(false);
    }

    @Test
    void getOperation_V1Operation_ReturnsResponse() {

        operation.setIun("iun123");

        Mockito.when(operationDAO.getByOperationId("op123"))
               .thenReturn(Mono.just(operation));

        StepVerifier.create(service.getOperation("op123"))
                    .assertNext(response -> {
                        assertNotNull(response);
                        assertEquals("WARNING", response.getStatus());
                        assertEquals("iun123", response.getIun());
                    })
                    .verifyComplete();
    }

    @Test
    void getOperation_WithSubOperations_ReturnsSubOperations() {

        operation.setSubOperationsIds(List.of("sub1", "sub2"));

        PnServiceDeskOperations sub1 = new PnServiceDeskOperations();
        sub1.setStatus("OK");
        sub1.setIun("iun1");
        sub1.setOperationId("sub1");

        PnServiceDeskOperations sub2 = new PnServiceDeskOperations();
        sub2.setStatus("KO");
        sub2.setErrorReason("ERROR");
        sub2.setIun("iun2");
        sub2.setOperationId("sub2");

        Mockito.when(operationDAO.getByOperationId("op123"))
               .thenReturn(Mono.just(operation));

        Mockito.when(operationDAO.getByOperationId("sub1"))
               .thenReturn(Mono.just(sub1));

        Mockito.when(operationDAO.getByOperationId("sub2"))
               .thenReturn(Mono.just(sub2));

        StepVerifier.create(service.getOperation("op123"))
                    .assertNext(response -> {
                        assertEquals(2, response.getSubOperations().size());

                        OperationDetail first = response.getSubOperations().get(0);
                        assertEquals("OK", first.getStatus());
                    })
                    .verifyComplete();
    }

    @Test
    void getOperation_NotFound_Returns404() {

        Mockito.when(operationDAO.getByOperationId("op123"))
               .thenReturn(Mono.empty());

        StepVerifier.create(service.getOperation("op123"))
                    .expectErrorMatches(ex -> {
                        assertTrue(ex instanceof PnGenericException);
                        assertEquals(OPERATION_IS_NOT_PRESENT,
                                     ((PnGenericException) ex).getExceptionType());
                        assertEquals(HttpStatus.NOT_FOUND,
                                     ((PnGenericException) ex).getHttpStatus());
                        return true;
                    })
                    .verify();
    }

    @Test
    void getOperation_IsSubOperation_Returns404() {

        operation.setIsSubOperation(true);

        Mockito.when(operationDAO.getByOperationId("op123"))
               .thenReturn(Mono.just(operation));

        StepVerifier.create(service.getOperation("op123"))
                    .expectError(PnGenericException.class)
                    .verify();
    }

    @Test
    void getOperation_WebClientException_ReturnsBadRequest() {

        Mockito.when(operationDAO.getByOperationId("op123"))
               .thenReturn(Mono.error(
                       new WebClientResponseException(
                               "Error",
                               HttpStatus.BAD_REQUEST.value(),
                               HttpStatus.BAD_REQUEST.getReasonPhrase(),
                               null,
                               null,
                               null)));

        StepVerifier.create(service.getOperation("op123"))
                    .expectErrorMatches(ex -> {
                        assertTrue(ex instanceof PnGenericException);
                        assertEquals(ERROR_DURING_GET_OPERATION_V2,
                                     ((PnGenericException) ex).getExceptionType());
                        assertEquals(HttpStatus.BAD_REQUEST,
                                     ((PnGenericException) ex).getHttpStatus());
                        return true;
                    })
                    .verify();
    }
}