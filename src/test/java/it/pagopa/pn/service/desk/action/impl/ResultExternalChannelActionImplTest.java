package it.pagopa.pn.service.desk.action.impl;

import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.dto.CourtesyMessageProgressEventDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.dto.ProgressEventCategoryDto;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.EXTERNALCHANNEL_STATUS_CODE_EMPTY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResultExternalChannelActionImplTest {

    @InjectMocks
    private ResultExternalChannelActionImpl resultExternalChannelAction;

    @Mock
    private OperationDAO operationDAO;

    private CourtesyMessageProgressEventDto getDto(String operationId, ProgressEventCategoryDto status) {
        CourtesyMessageProgressEventDto dto = new CourtesyMessageProgressEventDto();
        dto.setRequestId("SERVICE_DESK_OPID-" + operationId);
        dto.setStatus(status);
        return dto;
    }

    @Test
    void testExecute_entityNotFound() {
        CourtesyMessageProgressEventDto dto = getDto("NOT_FOUND", ProgressEventCategoryDto.OK);

        when(operationDAO.getByOperationId("NOT_FOUND")).thenReturn(Mono.empty());

        assertDoesNotThrow(() -> resultExternalChannelAction.execute(dto));
        verify(operationDAO, times(1)).getByOperationId("NOT_FOUND");
    }

    @Test
    void testExecute_statusCodeEmpty() {
        // Arrange
        CourtesyMessageProgressEventDto dto = getDto("QWERTY", null); // status è null
        PnServiceDeskOperations entity = new PnServiceDeskOperations();

        when(operationDAO.getByOperationId("QWERTY")).thenReturn(Mono.just(entity));

        assertDoesNotThrow(() -> resultExternalChannelAction.execute(dto));

        verify(operationDAO, never()).updateEntity(any());
    }

    @Test
    void testExecute_statusCodeOk() {
        CourtesyMessageProgressEventDto dto = getDto("OK123", ProgressEventCategoryDto.OK);
        PnServiceDeskOperations entity = new PnServiceDeskOperations();

        when(operationDAO.getByOperationId("OK123")).thenReturn(Mono.just(entity));
        when(operationDAO.updateEntity(any())).thenReturn(Mono.just(entity));
        when(operationDAO.updateEntity(any()).then()).thenReturn(Mono.empty());

        assertDoesNotThrow(() -> resultExternalChannelAction.execute(dto));
        verify(operationDAO, times(1)).updateEntity(any());
    }

    @Test
    void testExecute_statusCodeKo() {
        CourtesyMessageProgressEventDto dto = getDto("KO123", ProgressEventCategoryDto.ERROR);
        PnServiceDeskOperations entity = new PnServiceDeskOperations();

        when(operationDAO.getByOperationId("KO123")).thenReturn(Mono.just(entity));
        when(operationDAO.updateEntity(any())).thenReturn(Mono.just(entity));
        when(operationDAO.updateEntity(any()).then()).thenReturn(Mono.empty());

        assertDoesNotThrow(() -> resultExternalChannelAction.execute(dto));
        verify(operationDAO, times(1)).updateEntity(any());
    }

    @Test
    void testExecute_unexpectedError() {
        CourtesyMessageProgressEventDto dto = getDto("ERR123", ProgressEventCategoryDto.OK);

        when(operationDAO.getByOperationId("ERR123")).thenReturn(Mono.error(new RuntimeException("Boom")));

        assertDoesNotThrow(() -> resultExternalChannelAction.execute(dto));
        verify(operationDAO, times(1)).getByOperationId("ERR123");
    }

    @Test
    void testExecute_genericException() {
        CourtesyMessageProgressEventDto dto = getDto("GEN123", ProgressEventCategoryDto.OK);

        when(operationDAO.getByOperationId("GEN123")).thenReturn(Mono.error(new PnGenericException(EXTERNALCHANNEL_STATUS_CODE_EMPTY, "status code empty")));

        assertDoesNotThrow(() -> resultExternalChannelAction.execute(dto));
        verify(operationDAO, times(1)).getByOperationId("GEN123");
    }
}
