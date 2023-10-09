package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.CreateOperationRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.OperationResponse;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskEvents;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static it.pagopa.pn.service.desk.model.OperationStatusEnum.NOTIFY_VIEW;
import static it.pagopa.pn.service.desk.model.OperationStatusEnum.NOTIFY_VIEW_ERROR;
import static org.junit.jupiter.api.Assertions.*;

class OperationMapperTest {

    private final PnServiceDeskOperations pnServiceDeskOperations= new PnServiceDeskOperations();
    private final PnServiceDeskAttachments pnServiceDeskAttachments= new PnServiceDeskAttachments();
    private final PnServiceDeskConfigs pnServiceDeskConfigs= new PnServiceDeskConfigs();

    @BeforeEach
    public void inizialize(){
        PnServiceDeskConfigs.SenderAddress senderAddress = new PnServiceDeskConfigs.SenderAddress();
        senderAddress.setFullname("NAME");
        pnServiceDeskConfigs.setSenderAddress(senderAddress);
        pnServiceDeskConfigs.setSenderPaId("012");
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

        OperationResponse operationResponse= OperationMapper.operationResponseMapper(pnServiceDeskConfigs, pnServiceDeskOperations, "XYZ");
        assertNotNull(operationResponse);
        assertEquals(1, operationResponse.getIuns().size());
    }

    @Test
    void whenCalloperationResponseMapperAndAvailableIsFalse() {
        pnServiceDeskAttachments.setIsAvailable(false);

        List<PnServiceDeskAttachments> attachments = new ArrayList<>();
        attachments.add(pnServiceDeskAttachments);

        pnServiceDeskOperations.setAttachments(attachments);

        OperationResponse operationResponse= OperationMapper.operationResponseMapper(pnServiceDeskConfigs, pnServiceDeskOperations, "XYZ");
        assertNotNull(operationResponse);
        assertEquals(1, operationResponse.getUncompletedIuns().size());
    }

    @Test
    void whenCalloperationResponseMapperAndEvents() {
        Instant i = Instant.now();

        PnServiceDeskEvents pnServiceDeskEvents1 = new PnServiceDeskEvents();
        pnServiceDeskEvents1.setStatusDescription("Status 1");
        pnServiceDeskEvents1.setStatusCode("001");
        pnServiceDeskEvents1.setTimestamp(i.minus(2, ChronoUnit.HOURS));

        PnServiceDeskEvents pnServiceDeskEvents2= new PnServiceDeskEvents();
        pnServiceDeskEvents2.setStatusDescription("Status 2");
        pnServiceDeskEvents2.setStatusCode(null);
        pnServiceDeskEvents2.setTimestamp(null);

        PnServiceDeskEvents pnServiceDeskEvents3= new PnServiceDeskEvents();
        pnServiceDeskEvents3.setStatusDescription("Status 3");
        pnServiceDeskEvents3.setStatusCode("003");
        pnServiceDeskEvents3.setTimestamp(i);

        List<PnServiceDeskEvents> events = new ArrayList<>();
        events.addAll(List.of(pnServiceDeskEvents1, pnServiceDeskEvents2, pnServiceDeskEvents3));
        pnServiceDeskOperations.setEvents(events);

        OperationResponse operationResponse= OperationMapper.operationResponseMapper(pnServiceDeskConfigs, pnServiceDeskOperations, "XYZ");
        assertNotNull(operationResponse.getNotificationStatus());
        assertEquals(operationResponse.getNotificationStatus().getLastEventTimestamp().toInstant(), i);
        assertEquals("003", operationResponse.getNotificationStatus().getStatusCode());

    }

    @Test
    void whenCalloperationResponseMapperOperationStatusNotifyView() {
        pnServiceDeskAttachments.setIsAvailable(true);

        List<PnServiceDeskAttachments> attachments = new ArrayList<>();
        attachments.add(pnServiceDeskAttachments);

        pnServiceDeskOperations.setAttachments(attachments);
        pnServiceDeskOperations.setStatus(NOTIFY_VIEW.toString());

        OperationResponse operationResponse= OperationMapper.operationResponseMapper(pnServiceDeskConfigs, pnServiceDeskOperations, "XYZ");
        assertNotNull(operationResponse);
        assertEquals(OperationStatusEnum.OK.toString(),operationResponse.getNotificationStatus().getStatus().toString());

        pnServiceDeskOperations.setStatus(NOTIFY_VIEW_ERROR.toString());
        operationResponse= OperationMapper.operationResponseMapper(pnServiceDeskConfigs, pnServiceDeskOperations, "XYZ");
        assertEquals(OperationStatusEnum.OK.toString(),operationResponse.getNotificationStatus().getStatus().toString());
    }

    @Test
    void whenCalloperationResponseMapperOperationWithErrorReason() {
        pnServiceDeskAttachments.setIsAvailable(true);

        List<PnServiceDeskAttachments> attachments = new ArrayList<>();
        attachments.add(pnServiceDeskAttachments);

        pnServiceDeskOperations.setAttachments(attachments);
        pnServiceDeskOperations.setStatus("KO");
        pnServiceDeskOperations.setErrorReason("Error");
        pnServiceDeskOperations.setOperationLastUpdateDate(null);

        OperationResponse operationResponse= OperationMapper.operationResponseMapper(pnServiceDeskConfigs, pnServiceDeskOperations, "XYZ");
        assertEquals("KO",operationResponse.getNotificationStatus().getStatus().toString());
        assertEquals("Error",operationResponse.getNotificationStatus().getStatusDescription());
        assertNull(operationResponse.getOperationUpdateTimestamp());
    }

}