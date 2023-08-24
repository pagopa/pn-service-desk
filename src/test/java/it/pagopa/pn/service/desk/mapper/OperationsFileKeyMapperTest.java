package it.pagopa.pn.service.desk.mapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class OperationsFileKeyMapperTest {


    @Test
    void testNotNull(){
        assertNotNull(OperationsFileKeyMapper.getOperationFileKey("1234", "aboperation"));

    }
}
