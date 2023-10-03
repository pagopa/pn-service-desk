package it.pagopa.pn.service.desk.utility;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

}
