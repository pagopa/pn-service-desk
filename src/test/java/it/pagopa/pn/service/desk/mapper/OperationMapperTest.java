package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.CreateOperationRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.OperationResponse;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OperationMapperTest {

    private final PnServiceDeskOperations pnServiceDeskOperations= new PnServiceDeskOperations();
    private final PnServiceDeskAttachments pnServiceDeskAttachments= new PnServiceDeskAttachments();

    @BeforeEach
    public void inizialize(){
        pnServiceDeskAttachments.setIun("123");
        pnServiceDeskAttachments.setFilesKey(new ArrayList<>());

        pnServiceDeskOperations.setOperationId("123");
        pnServiceDeskOperations.setOperationStartDate(Instant.now());
        pnServiceDeskOperations.setOperationLastUpdateDate(Instant.now());
        pnServiceDeskOperations.setStatus("OK");
    }

    @Test
    void getInitialOperation() {
        CreateOperationRequest operationRequest= new CreateOperationRequest();
        operationRequest.setTicketOperationId("1234");
        operationRequest.setTicketId("1234");
        operationRequest.setTaxId("taxId");

        PnServiceDeskOperations pnServiceDeskOperations= OperationMapper.getInitialOperation(operationRequest, "1234");
        assertNotNull(pnServiceDeskOperations);
        assertEquals(pnServiceDeskOperations.getOperationId(), operationRequest.getTicketId().concat(operationRequest.getTicketOperationId()));
        assertEquals(pnServiceDeskOperations.getStatus(), OperationStatusEnum.CREATING.toString());
    }
    @Test
    void whenCallgetInitialOperationAndTicketOperationIdIsNull() {
        CreateOperationRequest operationRequest= new CreateOperationRequest();
        operationRequest.setTicketId("1234");
        operationRequest.setTaxId("taxId");

        PnServiceDeskOperations pnServiceDeskOperations= OperationMapper.getInitialOperation(operationRequest, "1234");
        assertNotNull(pnServiceDeskOperations);
        assertEquals(pnServiceDeskOperations.getOperationId(), operationRequest.getTicketId().concat("000"));

    }

    @Test
    void whenCalloperationResponseMapperAndAvailableIsTrue() {
        pnServiceDeskAttachments.setIsAvailable(true);

        List<PnServiceDeskAttachments> attachments = new ArrayList<>();
        attachments.add(pnServiceDeskAttachments);

        pnServiceDeskOperations.setAttachments(attachments);

        OperationResponse operationResponse= OperationMapper.operationResponseMapper(pnServiceDeskOperations);
        assertNotNull(operationResponse);
        assertEquals(operationResponse.getIuns().size(), 1);
    }

    @Test
    void whenCalloperationResponseMapperAndAvailableIsFalse() {
        pnServiceDeskAttachments.setIsAvailable(false);

        List<PnServiceDeskAttachments> attachments = new ArrayList<>();
        attachments.add(pnServiceDeskAttachments);

        pnServiceDeskOperations.setAttachments(attachments);

        OperationResponse operationResponse= OperationMapper.operationResponseMapper(pnServiceDeskOperations);
        assertNotNull(operationResponse);
        assertEquals(operationResponse.getUncompletedIuns().size(), 1);
    }

}