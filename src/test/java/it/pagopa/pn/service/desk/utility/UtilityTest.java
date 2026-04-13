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
        HttpStatusCode result = Utility.convertToHttpStatus(statusCode);
        Assertions.assertEquals(HttpStatus.OK, result);
    }

    @Test
    void testConvertToHttpStatus_NonStandardCode() {
        HttpStatusCode statusCode = HttpStatusCode.valueOf(999);
        HttpStatusCode result = Utility.convertToHttpStatus(statusCode);
        Assertions.assertEquals(999, result.value());
    }

    @Test
    void testResolveAddressOperationId_subOperation_extractsParentId() {
        // SUB#{parentOperationId}#{iun} → parentOperationId
        String result = Utility.resolveAddressOperationId("SUB#ticket123op1#ABCD-1234-EFGH-5678");
        Assertions.assertEquals("ticket123op1", result);
    }

    @Test
    void testResolveAddressOperationId_regularOperation_behavesLikeCleanUp() {
        // V1/V2 parent with -SENT- suffix → stripped just like cleanUpOperationId
        String result = Utility.resolveAddressOperationId("TEST-new-UN-6test-unre-SENT-1");
        Assertions.assertEquals("TEST-new-UN-6test-unre", result);
    }

    @Test
    void testResolveAddressOperationId_regularOperationNoSuffix_returnedAsIs() {
        // V1/V2 parent without -SENT- suffix → unchanged
        String result = Utility.resolveAddressOperationId("ticket123op1");
        Assertions.assertEquals("ticket123op1", result);
    }

    @Test
    void testResolveAddressOperationId_subOperationWithoutIunSeparator_returnsWithoutPrefix() {
        // Edge case: "SUB#parentId" without second # → returns parentId
        String result = Utility.resolveAddressOperationId("SUB#ticket123op1");
        Assertions.assertEquals("ticket123op1", result);
    }

}
