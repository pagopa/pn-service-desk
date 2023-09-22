package it.pagopa.pn.service.desk.action.impl;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.SendEventDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.StatusCodeEnumDto;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskEvents;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.queue.model.InternalEvent;
import it.pagopa.pn.service.desk.middleware.queue.producer.InternalQueueMomProducer;
import it.pagopa.pn.service.desk.utility.Utility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.PAPERCHANNEL_STATUS_CODE_EMPTY;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class ResultPaperChannelActionTest {

    @InjectMocks
    private ResultPaperChannelActionImpl resultPaperChannelAction;

    @Mock
    private OperationDAO operationDAO;

    @Mock
    private InternalQueueMomProducer internalQueueMomProducer;



    @Test
    void executeCaseExceptionEntityNotFound() {
        SendEventDto sendEventDto = getSendEventDto();
        Mockito.when(operationDAO.getByOperationId(Mockito.any()))
                .thenReturn(Mono.empty());
        assertDoesNotThrow(() -> resultPaperChannelAction.execute(sendEventDto));
    }

    @Test
    void executeCaseExceptionStatusCodeEmpty() {
        SendEventDto sendEventDto = getSendEventDto();
        PnServiceDeskOperations entity = new PnServiceDeskOperations();

        List<PnServiceDeskEvents> serviceDeskEvents = new ArrayList<>();
        serviceDeskEvents.add(new PnServiceDeskEvents());
        entity.setEvents(serviceDeskEvents);

        Mockito.when(operationDAO.getByOperationId("QWERTY"))
                .thenReturn(Mono.just(entity));
        Mockito.when(operationDAO.updateEntity(entity))
                .thenReturn(Mono.just(entity));
        Mockito.when(operationDAO.updateEntity(entity).then())
                .thenReturn(Mono.empty());

        assertDoesNotThrow(() -> resultPaperChannelAction.execute(sendEventDto));

        ArgumentCaptor<PnServiceDeskOperations> captorPnServiceDeskOperations = ArgumentCaptor.forClass(PnServiceDeskOperations.class);
        Mockito.verify(operationDAO, Mockito.timeout(1000).times(1))
                .updateEntity(captorPnServiceDeskOperations.capture());

        assertNotNull(captorPnServiceDeskOperations.getValue());
        assertEquals(PAPERCHANNEL_STATUS_CODE_EMPTY.getMessage(),
                Objects.requireNonNull(captorPnServiceDeskOperations.getValue().getErrorReason()));
    }

    @Test
    void executeCaseExceptionStatusCodeNotEmpty() {
        SendEventDto sendEventDto = getSendEventDto();
        sendEventDto.setStatusCode(StatusCodeEnumDto.OK);

        PnServiceDeskOperations entity = new PnServiceDeskOperations();

        List<PnServiceDeskEvents> serviceDeskEvents = new ArrayList<>();
        serviceDeskEvents.add(new PnServiceDeskEvents());
        entity.setEvents(serviceDeskEvents);

        Mockito.when(operationDAO.getByOperationId("QWERTY"))
                .thenReturn(Mono.just(entity));
        Mockito.when(operationDAO.updateEntity(entity))
                .thenReturn(Mono.just(entity));
        Mockito.when(operationDAO.updateEntity(entity).then())
                .thenReturn(Mono.empty());

        assertDoesNotThrow(() -> resultPaperChannelAction.execute(sendEventDto));
    }

    @Test
    void executeCaseUpdateNotificationViewed() {
        SendEventDto sendEventDto = getSendEventDtoNotificationViewer();
        sendEventDto.setStatusCode(StatusCodeEnumDto.OK);

        PnServiceDeskOperations entity = new PnServiceDeskOperations();

        List<PnServiceDeskEvents> serviceDeskEvents = new ArrayList<>();
        serviceDeskEvents.add(new PnServiceDeskEvents());
        entity.setEvents(serviceDeskEvents);
        entity.setAttachments(getAttachments());

        Mockito.when(operationDAO.getByOperationId("QWERTY"))
                .thenReturn(Mono.just(entity));

        Mockito.when(operationDAO.updateEntity(entity))
                .thenReturn(Mono.just(entity));

        Mockito.doNothing().when(internalQueueMomProducer)
                .push((InternalEvent) Mockito.any());

        assertDoesNotThrow(() -> resultPaperChannelAction.execute(sendEventDto));
    }

    @Test
    void executeCaseUpdateNotificationViewedEmptyIuns() {
        SendEventDto sendEventDto = getSendEventDtoNotificationViewer();
        sendEventDto.setStatusCode(StatusCodeEnumDto.OK);

        PnServiceDeskOperations entity = new PnServiceDeskOperations();

        List<PnServiceDeskEvents> serviceDeskEvents = new ArrayList<>();
        serviceDeskEvents.add(new PnServiceDeskEvents());
        entity.setEvents(serviceDeskEvents);
        entity.setAttachments(getAttachmentsEmptyIuns());

        Mockito.when(operationDAO.getByOperationId("QWERTY"))
                .thenReturn(Mono.just(entity));

        Mockito.when(operationDAO.updateEntity(entity))
                .thenReturn(Mono.just(entity));

        assertDoesNotThrow(() -> resultPaperChannelAction.execute(sendEventDto));
    }

    @Test
    void executeCaseExceptionStatusCodeKoUnreachable() {
        SendEventDto sendEventDto = getSendEventDto();
        sendEventDto.setStatusCode(StatusCodeEnumDto.KOUNREACHABLE);

        PnServiceDeskOperations entity = new PnServiceDeskOperations();

        List<PnServiceDeskEvents> serviceDeskEvents = new ArrayList<>();
        serviceDeskEvents.add(new PnServiceDeskEvents());
        entity.setEvents(serviceDeskEvents);

        Mockito.when(operationDAO.getByOperationId("QWERTY"))
                .thenReturn(Mono.just(entity));
        Mockito.when(operationDAO.updateEntity(entity))
                .thenReturn(Mono.just(entity));
        Mockito.when(operationDAO.updateEntity(entity).then())
                .thenReturn(Mono.empty());

        assertDoesNotThrow(() -> resultPaperChannelAction.execute(sendEventDto));

        ArgumentCaptor<PnServiceDeskOperations> captorPnServiceDeskOperations = ArgumentCaptor.forClass(PnServiceDeskOperations.class);
        Mockito.verify(operationDAO, Mockito.timeout(1000).times(1))
                .updateEntity(captorPnServiceDeskOperations.capture());

        assertNotNull(captorPnServiceDeskOperations.getValue());
        assertEquals(Utility.getOperationStatusFrom(StatusCodeEnumDto.KO).toString(),
                Objects.requireNonNull(captorPnServiceDeskOperations.getValue().getStatus()));
    }

    private SendEventDto getSendEventDto() {
        String requestId = "SERVICE_DESK_OPID-QWERTY";
        SendEventDto sendEventDto = new SendEventDto();
        sendEventDto.setRequestId(requestId);
        sendEventDto.setStatusDetail("");
        sendEventDto.setStatusDateTime(OffsetDateTime.ofInstant(Instant.now(), ZoneOffset.UTC));
        sendEventDto.setClientRequestTimeStamp(OffsetDateTime.ofInstant(Instant.now(), ZoneOffset.UTC));
        sendEventDto.setStatusDescription("");
        sendEventDto.setDeliveryFailureCause("");
        return sendEventDto;
    }

    private SendEventDto getSendEventDtoNotificationViewer() {
        String requestId = "SERVICE_DESK_OPID-QWERTY";
        SendEventDto sendEventDto = new SendEventDto();
        sendEventDto.setRequestId(requestId);
        sendEventDto.setStatusCode(StatusCodeEnumDto.OK);
        sendEventDto.setStatusDetail("");
        sendEventDto.setStatusDateTime(OffsetDateTime.ofInstant(Instant.now(), ZoneOffset.UTC));
        sendEventDto.setClientRequestTimeStamp(OffsetDateTime.ofInstant(Instant.now(), ZoneOffset.UTC));
        sendEventDto.setStatusDescription("");
        sendEventDto.setDeliveryFailureCause("");
        return sendEventDto;
    }

    private List<PnServiceDeskAttachments> getAttachments (){
        List<PnServiceDeskAttachments> attachments = new ArrayList<>();
        PnServiceDeskAttachments attachment = new PnServiceDeskAttachments();
        attachment.setIun("LJLH-GNTJ-DVXR-202209-J-1");
        attachment.setIsAvailable(true);
        List<String> fileKeys = new ArrayList<>();
        String fileKey = "safestorage://981234";
        fileKeys.add(fileKey);
        attachment.setFilesKey(fileKeys);
        attachments.add(attachment);
        return attachments;
    }

    private List<PnServiceDeskAttachments> getAttachmentsEmptyIuns (){
        List<PnServiceDeskAttachments> attachments = new ArrayList<>();
        PnServiceDeskAttachments attachment = new PnServiceDeskAttachments();
        attachment.setIsAvailable(true);
        attachment.setIun(null);
        List<String> fileKeys = new ArrayList<>();
        String fileKey = "safestorage://981234";
        fileKeys.add(fileKey);
        attachment.setFilesKey(fileKeys);
        attachments.add(attachment);
        return attachments;
    }
}
