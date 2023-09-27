package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.model.OperationDto;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OperationDtoMapperTest {

    @Test
    void initOperation() {
        PnServiceDeskOperations p = new PnServiceDeskOperations();
        p.setStatus(OperationStatusEnum.KO.toString());
        p.setOperationId("1");
        OperationDto dto = OperationDtoMapper.initOperation(p, "123");
        assertNotNull(dto);
        assertEquals(p.getOperationId(), dto.getOperationId());
        assertEquals("123", dto.getIun());
    }

}