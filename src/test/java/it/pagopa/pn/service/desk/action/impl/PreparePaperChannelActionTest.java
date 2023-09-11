package it.pagopa.pn.service.desk.action.impl;

import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.*;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskEvents;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault.PnDataVaultClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.paperchannel.PnPaperChannelClient;
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

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_DURING_PAPER_SEND;
import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.PAPERCHANNEL_STATUS_CODE_EMPTY;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class PreparePaperChannelActionTest {

    @InjectMocks
    private PreparePaperChannelActionImpl preparePaperChannelAction;

    @Mock
    private OperationDAO operationDAO;

    @Mock
    private PnPaperChannelClient paperChannelClient;

    @Mock
    private PnServiceDeskConfigs pnServiceDeskConfigs;
    @Mock
    private PnDataVaultClient pnDataVaultClient;


    @Test
    void executeCaseExceptionEntityNotFound() {
        PrepareEventDto prepareEventDto = getPrepareEventDto();
        Mockito.when(operationDAO.getByOperationId(Mockito.any())).thenReturn(Mono.empty());
        assertDoesNotThrow(() -> preparePaperChannelAction.execute(prepareEventDto));
    }

    @Test
    void executeCaseExceptionStatusCodeEmpty() {
        PrepareEventDto prepareEventDto = getPrepareEventDto();
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

        assertDoesNotThrow(() -> preparePaperChannelAction.execute(prepareEventDto));

        ArgumentCaptor<PnServiceDeskOperations> captorPnServiceDeskOperations = ArgumentCaptor.forClass(PnServiceDeskOperations.class);
        Mockito.verify(operationDAO, Mockito.timeout(1000).times(1))
                .updateEntity(captorPnServiceDeskOperations.capture());

        assertNotNull(captorPnServiceDeskOperations.getValue());
        assertEquals(PAPERCHANNEL_STATUS_CODE_EMPTY.getMessage(),
                Objects.requireNonNull(captorPnServiceDeskOperations.getValue().getErrorReason()));
    }

    @Test
    void executeCaseStatusCodeProgress() {
        PnServiceDeskConfigs pnsdc = getPnServiceDeskConfigs();
        PrepareEventDto prepareEventDto = getPrepareEventDto();
        prepareEventDto.setStatusCode(StatusCodeEnumDto.PROGRESS);

        PnServiceDeskOperations entity = new PnServiceDeskOperations();
        List<PnServiceDeskEvents> serviceDeskEvents = new ArrayList<>();
        serviceDeskEvents.add(new PnServiceDeskEvents());
        entity.setEvents(serviceDeskEvents);

        entity.setOperationId("QWERTY");
        entity.setStatus(StatusCodeEnumDto.PROGRESS.toString());

        List<PnServiceDeskAttachments> attachmentsList = new ArrayList<>();
        attachmentsList.add(new PnServiceDeskAttachments());
        entity.setAttachments(attachmentsList);


        Mockito.when(pnServiceDeskConfigs.getSenderPaId())
                .thenReturn(pnsdc.getSenderPaId());
        Mockito.when(pnServiceDeskConfigs.getSenderAddress())
                .thenReturn(pnsdc.getSenderAddress());
        Mockito.when(paperChannelClient.sendPaperSendRequest(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(new SendResponseDto()));

        Mockito.when(operationDAO.getByOperationId("QWERTY"))
                .thenReturn(Mono.just(entity));
        Mockito.when(operationDAO.updateEntity(entity))
                .thenReturn(Mono.just(entity));
        Mockito.when(operationDAO.updateEntity(entity).then())
                .thenReturn(Mono.empty());
        Mockito.when(pnDataVaultClient.deAnonymized(Mockito.any()))
                .thenReturn(Mono.just("MCCLLSS332423"));

        assertDoesNotThrow(() -> preparePaperChannelAction.execute(prepareEventDto));

        ArgumentCaptor<String> captorString = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<SendRequestDto> captorSendRequestDto = ArgumentCaptor.forClass(SendRequestDto.class);

        Mockito.verify(paperChannelClient, Mockito.timeout(1000).times(1))
                .sendPaperSendRequest(captorString.capture(), captorSendRequestDto.capture());

        assertNotNull(captorString.getValue());
        assertNotNull(captorSendRequestDto.getValue());
        assertEquals(prepareEventDto.getRequestId(),
                Objects.requireNonNull(captorSendRequestDto.getValue().getRequestId()));
    }

    @Test
    void executeCaseStatusCodePaperSendError() {
        PnServiceDeskConfigs pnsdc = getPnServiceDeskConfigs();
        PrepareEventDto prepareEventDto = getPrepareEventDto();
        prepareEventDto.setStatusCode(StatusCodeEnumDto.PROGRESS);

        PnServiceDeskOperations entity = new PnServiceDeskOperations();
        List<PnServiceDeskEvents> serviceDeskEvents = new ArrayList<>();
        serviceDeskEvents.add(new PnServiceDeskEvents());
        entity.setEvents(serviceDeskEvents);
        entity.setOperationId("QWERTY");
        entity.setStatus(StatusCodeEnumDto.PROGRESS.toString());

        List<PnServiceDeskAttachments> attachmentsList = new ArrayList<>();
        attachmentsList.add(new PnServiceDeskAttachments());
        entity.setAttachments(attachmentsList);

        Mockito.when(pnServiceDeskConfigs.getSenderPaId())
                .thenReturn(pnsdc.getSenderPaId());
        Mockito.when(pnServiceDeskConfigs.getSenderAddress())
                .thenReturn(pnsdc.getSenderAddress());
        Mockito.when(paperChannelClient.sendPaperSendRequest(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.error(new PnGenericException(ERROR_DURING_PAPER_SEND, ERROR_DURING_PAPER_SEND.getMessage())));
        Mockito.when(operationDAO.getByOperationId("QWERTY"))
                .thenReturn(Mono.just(entity));
        Mockito.when(operationDAO.updateEntity(entity))
                .thenReturn(Mono.just(entity));
        Mockito.when(operationDAO.updateEntity(entity).then())
                .thenReturn(Mono.empty());
        Mockito.when(pnDataVaultClient.deAnonymized(Mockito.any()))
                .thenReturn(Mono.just("MCCLLSS332423"));

        assertDoesNotThrow(() -> preparePaperChannelAction.execute(prepareEventDto));
    }

    @Test
    void executeCaseStatusCodeNotProgress() {
        PrepareEventDto prepareEventDto = getPrepareEventDto();
        prepareEventDto.setStatusCode(StatusCodeEnumDto.OK);

        PnServiceDeskOperations entity = new PnServiceDeskOperations();
        List<PnServiceDeskEvents> serviceDeskEvents = new ArrayList<>();
        serviceDeskEvents.add(new PnServiceDeskEvents());
        entity.setEvents(serviceDeskEvents);
        entity.setOperationId("QWERTY");
        entity.setStatus(StatusCodeEnumDto.OK.toString());

        List<PnServiceDeskAttachments> attachmentsList = new ArrayList<>();
        attachmentsList.add(new PnServiceDeskAttachments());
        entity.setAttachments(attachmentsList);

        Mockito.when(operationDAO.getByOperationId("QWERTY"))
                .thenReturn(Mono.just(entity));
        Mockito.when(operationDAO.updateEntity(entity))
                .thenReturn(Mono.just(entity));
        Mockito.when(operationDAO.updateEntity(entity).then())
                .thenReturn(Mono.empty());

        assertDoesNotThrow(() -> preparePaperChannelAction.execute(prepareEventDto));
    }

    private PrepareEventDto getPrepareEventDto() {
        String requestId = "SERVICE_DESK_OPID-QWERTY";
        PrepareEventDto prepareEventDto = new PrepareEventDto();
        prepareEventDto.setRequestId(requestId);
        prepareEventDto.setProductType("890");
        prepareEventDto.setStatusDateTime(OffsetDateTime.ofInstant(Instant.now(), ZoneOffset.UTC));
        prepareEventDto.setStatusDetail("");
        AnalogAddressDto analogAddressDto = new AnalogAddressDto();
        analogAddressDto.setFullname("Ettore Fieramosca");
        analogAddressDto.setAddress("Via Giolitti");
        analogAddressDto.setCap("00100");
        analogAddressDto.setCity("Roma");
        analogAddressDto.setPr("Roma");
        analogAddressDto.setCountry("Italia");
        prepareEventDto.setReceiverAddress(analogAddressDto);
        return prepareEventDto;
    }

    private PnServiceDeskConfigs getPnServiceDeskConfigs() {
        PnServiceDeskConfigs pnsdc = new PnServiceDeskConfigs();
        pnsdc.setSenderPaId("0123456789");
        PnServiceDeskConfigs.SenderAddress senderAddress = new PnServiceDeskConfigs.SenderAddress();
        senderAddress.setFullname("Accenture S.R.L.");
        senderAddress.setAddress("Via Sciangai");
        senderAddress.setZipcode("00100");
        senderAddress.setCity("Roma");
        senderAddress.setPr("Roma");
        senderAddress.setCountry("Italia");
        pnsdc.setSenderAddress(senderAddress);
        return pnsdc;
    }
}
