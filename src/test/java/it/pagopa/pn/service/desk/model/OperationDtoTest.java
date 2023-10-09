package it.pagopa.pn.service.desk.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OperationDtoTest {

    @Test
    void equalsTest() {
        OperationDto op1 = new OperationDto();
        OperationDto op2 = new OperationDto();
        op1.setIun("iun1");
        op2.setIun("iun1");
        Assertions.assertEquals(op1, op2);

        op1.setStatus(OperationStatusEnum.OK.toString());
        op2.setStatus(OperationStatusEnum.OK.toString());
        Assertions.assertEquals(op1, op2);

        op2.setStatus(OperationStatusEnum.KO.toString());
        Assertions.assertEquals(op1, op2);

        op2.setIun("iun2");
        Assertions.assertNotEquals(op1, op2);
    }

}
