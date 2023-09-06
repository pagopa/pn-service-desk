package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.CreateOperationRequest;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OperationMapperTest {

    @Test
    void getInitialOperation() {
        CreateOperationRequest operationRequest= new CreateOperationRequest();
        operationRequest.setTicketOperationId("1234");
        operationRequest.setTicketId("1234");
        operationRequest.setTaxId("taxId");

        PnServiceDeskOperations pnServiceDeskOperations= OperationMapper.getInitialOperation(operationRequest, "1234");
        assertNotNull(pnServiceDeskOperations);
    }

}