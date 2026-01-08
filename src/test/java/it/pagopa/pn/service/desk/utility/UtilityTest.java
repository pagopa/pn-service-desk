package it.pagopa.pn.service.desk.utility;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

class UtilityTest {


    @Test
    void testGenerateRequestId(){
        String requestId = Utility.generateRequestId("12345");
        Assertions.assertEquals("SERVICE_DESK_OPID-12345", requestId);
    }

    @Test
    void testExtractOperationId(){
        String operationId = Utility.extractOperationId("SERVICE_DESK_OPID-12345");
        Assertions.assertEquals("12345", operationId);

        operationId = Utility.extractOperationId("1234556");
        Assertions.assertNull(operationId);
    }

    @Test
    void testGenerateOperationId(){
        String operationId = Utility.generateOperationId("1234", null);
        Assertions.assertEquals("1234000", operationId);

        operationId = Utility.generateOperationId("1234", "     ");
        Assertions.assertEquals("1234000", operationId);

        operationId = Utility.generateOperationId("1234","a-dfr");
        Assertions.assertEquals("1234a-dfr", operationId);
    }

    @Test
    void testCleanUpOperationId(){
        String operationId = Utility.cleanUpOperationId("TEST-new-UN-6test-unre-SENT-1");
        Assertions.assertEquals("TEST-new-UN-6test-unre", operationId);

        operationId = Utility.cleanUpOperationId("TEST-new-UN-6test-unre");
        Assertions.assertEquals("TEST-new-UN-6test-unre", operationId);
    }

    @Test
    void testConvertToHttpStatus_Success() {
        HttpStatusCode statusCode = HttpStatus.OK;
        HttpStatus result = Utility.convertToHttpStatus(statusCode);
        Assertions.assertEquals(HttpStatus.OK, result);
    }

    @Test
    void testConvertToHttpStatus_InvalidCode() {
        HttpStatusCode statusCode = HttpStatusCode.valueOf(999);
        HttpStatus result = Utility.convertToHttpStatus(statusCode);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result);
    }

}
