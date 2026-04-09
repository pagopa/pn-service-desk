package it.pagopa.pn.service.desk.action.impl;

import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.dto.CourtesyMessageProgressEventDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.dto.ProgressEventCategoryDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.dto.SingleStatusUpdateDto;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.List;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.EXTERNALCHANNEL_STATUS_CODE_EMPTY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResultExternalChannelActionImplTest {

    @InjectMocks
    private ResultExternalChannelActionImpl resultExternalChannelAction;

    @Mock
    private OperationDAO operationDAO;

    @Mock
    private PnServiceDeskConfigs cfg;

    @BeforeEach
    void setup() {
        Mockito.lenient().when(cfg.getExternalChannelDigitalCodesSuccess()).thenReturn(List.of(CourtesyMessageProgressEventDto.EventCodeEnum.M003.getValue()));
        Mockito.lenient().when(cfg.getExternalChannelDigitalCodesFailure()).thenReturn(List.of(CourtesyMessageProgressEventDto.EventCodeEnum.M008.getValue()));
    }

    private SingleStatusUpdateDto getDto(String operationId, ProgressEventCategoryDto status, CourtesyMessageProgressEventDto.EventCodeEnum eventCode) {
        SingleStatusUpdateDto singleStatusUpdateDto = new SingleStatusUpdateDto();

        CourtesyMessageProgressEventDto dto = new CourtesyMessageProgressEventDto();
        dto.setRequestId("SERVICE_DESK_OPID-" + operationId);
        dto.setStatus(status);
        dto.setEventCode(eventCode);

        singleStatusUpdateDto.setDigitalCourtesy(dto);
        return singleStatusUpdateDto;
    }

    @Test
    void testExecute_entityNotFound() {
        SingleStatusUpdateDto dto = getDto("NOT_FOUND", ProgressEventCategoryDto.OK, CourtesyMessageProgressEventDto.EventCodeEnum.M003);

        when(operationDAO.getByOperationId("NOT_FOUND")).thenReturn(Mono.empty());

        assertDoesNotThrow(() -> resultExternalChannelAction.execute(dto));
        verify(operationDAO, times(1)).getByOperationId("NOT_FOUND");
    }

    @Test
    void testExecute_statusCodeEmpty() {
        // Arrange
        SingleStatusUpdateDto dto = getDto("QWERTY", null, null); // status è null
        PnServiceDeskOperations entity = new PnServiceDeskOperations();

        when(operationDAO.getByOperationId("QWERTY")).thenReturn(Mono.just(entity));

        assertDoesNotThrow(() -> resultExternalChannelAction.execute(dto));

        verify(operationDAO, never()).updateEntity(any());
    }

    @Test
    void testExecute_statusCodeOk() {
        SingleStatusUpdateDto dto = getDto("OK123", ProgressEventCategoryDto.OK, CourtesyMessageProgressEventDto.EventCodeEnum.M003);
        PnServiceDeskOperations entity = new PnServiceDeskOperations();

        when(operationDAO.getByOperationId("OK123")).thenReturn(Mono.just(entity));
        when(operationDAO.updateEntity(any())).thenReturn(Mono.just(entity));
        when(operationDAO.updateEntity(any()).then()).thenReturn(Mono.empty());

        assertDoesNotThrow(() -> resultExternalChannelAction.execute(dto));
        verify(operationDAO, times(1)).updateEntity(any());
    }

    @Test
    void testExecute_statusCodeKo() {
        SingleStatusUpdateDto dto = getDto("KO123", ProgressEventCategoryDto.ERROR, CourtesyMessageProgressEventDto.EventCodeEnum.M008);
        PnServiceDeskOperations entity = new PnServiceDeskOperations();

        when(operationDAO.getByOperationId("KO123")).thenReturn(Mono.just(entity));
        when(operationDAO.updateEntity(any())).thenReturn(Mono.just(entity));
        when(operationDAO.updateEntity(any()).then()).thenReturn(Mono.empty());

        assertDoesNotThrow(() -> resultExternalChannelAction.execute(dto));
        verify(operationDAO, times(1)).updateEntity(any());
    }

    @Test
    void testExecute_unexpectedError() {
        SingleStatusUpdateDto dto = getDto("ERR123", ProgressEventCategoryDto.OK, CourtesyMessageProgressEventDto.EventCodeEnum.M003);

        when(operationDAO.getByOperationId("ERR123")).thenReturn(Mono.error(new RuntimeException("Boom")));

        assertDoesNotThrow(() -> resultExternalChannelAction.execute(dto));
        verify(operationDAO, times(1)).getByOperationId("ERR123");
    }

    @Test
    void testExecute_genericException() {
        SingleStatusUpdateDto dto = getDto("GEN123", ProgressEventCategoryDto.OK, CourtesyMessageProgressEventDto.EventCodeEnum.M003);

        when(operationDAO.getByOperationId("GEN123")).thenReturn(Mono.error(new PnGenericException(EXTERNALCHANNEL_STATUS_CODE_EMPTY, "status code empty")));

        assertDoesNotThrow(() -> resultExternalChannelAction.execute(dto));
        verify(operationDAO, times(1)).getByOperationId("GEN123");
    }

    @Test
    void testExecute_subOp_allOk_updatesParentOk() {
        String subOpId = "SUB#parentId#IUN-001";
        SingleStatusUpdateDto dto = getDto(subOpId, ProgressEventCategoryDto.OK, CourtesyMessageProgressEventDto.EventCodeEnum.M003);

        PnServiceDeskOperations subOp = new PnServiceDeskOperations();
        subOp.setOperationId(subOpId);
        subOp.setIsSubOperation(Boolean.TRUE);

        PnServiceDeskOperations sibling = new PnServiceDeskOperations();
        sibling.setOperationId("SUB#parentId#IUN-002");
        sibling.setStatus(OperationStatusEnum.OK.name());

        PnServiceDeskOperations parent = new PnServiceDeskOperations();
        parent.setOperationId("parentId");
        parent.setSubOperationsIds(List.of(subOpId, "SUB#parentId#IUN-002"));

        when(operationDAO.getByOperationId(subOpId)).thenReturn(Mono.just(subOp));
        when(operationDAO.updateEntity(subOp)).thenReturn(Mono.just(subOp));
        when(operationDAO.getByOperationId("parentId")).thenReturn(Mono.just(parent));
        when(operationDAO.getByOperationId("SUB#parentId#IUN-002")).thenReturn(Mono.just(sibling));
        when(operationDAO.updateEntity(parent)).thenReturn(Mono.just(parent));

        assertDoesNotThrow(() -> resultExternalChannelAction.execute(dto));

        ArgumentCaptor<PnServiceDeskOperations> captor = ArgumentCaptor.forClass(PnServiceDeskOperations.class);
        verify(operationDAO, atLeastOnce()).updateEntity(captor.capture());
        boolean parentUpdatedOk = captor.getAllValues().stream()
                .anyMatch(op -> "parentId".equals(op.getOperationId()) && OperationStatusEnum.OK.name().equals(op.getStatus()));
        assertTrue(parentUpdatedOk, "Parent operation should be updated to OK");
    }

    @Test
    void testExecute_subOp_mixedResult_updatesParentWarning() {
        String subOpId = "SUB#parentId#IUN-001";
        SingleStatusUpdateDto dto = getDto(subOpId, ProgressEventCategoryDto.OK, CourtesyMessageProgressEventDto.EventCodeEnum.M003);

        PnServiceDeskOperations subOp = new PnServiceDeskOperations();
        subOp.setOperationId(subOpId);
        subOp.setIsSubOperation(Boolean.TRUE);

        PnServiceDeskOperations sibling = new PnServiceDeskOperations();
        sibling.setOperationId("SUB#parentId#IUN-002");
        sibling.setStatus(OperationStatusEnum.KO.name());

        PnServiceDeskOperations parent = new PnServiceDeskOperations();
        parent.setOperationId("parentId");
        parent.setSubOperationsIds(List.of(subOpId, "SUB#parentId#IUN-002"));

        when(operationDAO.getByOperationId(subOpId)).thenReturn(Mono.just(subOp));
        when(operationDAO.updateEntity(subOp)).thenReturn(Mono.just(subOp));
        when(operationDAO.getByOperationId("parentId")).thenReturn(Mono.just(parent));
        when(operationDAO.getByOperationId("SUB#parentId#IUN-002")).thenReturn(Mono.just(sibling));
        when(operationDAO.updateEntity(parent)).thenReturn(Mono.just(parent));

        assertDoesNotThrow(() -> resultExternalChannelAction.execute(dto));

        ArgumentCaptor<PnServiceDeskOperations> captor = ArgumentCaptor.forClass(PnServiceDeskOperations.class);
        verify(operationDAO, atLeastOnce()).updateEntity(captor.capture());
        boolean parentUpdatedWarning = captor.getAllValues().stream()
                .anyMatch(op -> "parentId".equals(op.getOperationId()) && OperationStatusEnum.WARNING.name().equals(op.getStatus()));
        assertTrue(parentUpdatedWarning, "Parent operation should be updated to WARNING");
    }

    @Test
    void testExecute_subOp_allKo_updatesParentKo() {
        String subOpId = "SUB#parentId#IUN-001";
        SingleStatusUpdateDto dto = getDto(subOpId, ProgressEventCategoryDto.ERROR, CourtesyMessageProgressEventDto.EventCodeEnum.M008);

        PnServiceDeskOperations subOp = new PnServiceDeskOperations();
        subOp.setOperationId(subOpId);
        subOp.setIsSubOperation(Boolean.TRUE);

        PnServiceDeskOperations sibling = new PnServiceDeskOperations();
        sibling.setOperationId("SUB#parentId#IUN-002");
        sibling.setStatus(OperationStatusEnum.KO.name());

        PnServiceDeskOperations parent = new PnServiceDeskOperations();
        parent.setOperationId("parentId");
        parent.setSubOperationsIds(List.of(subOpId, "SUB#parentId#IUN-002"));

        when(operationDAO.getByOperationId(subOpId)).thenReturn(Mono.just(subOp));
        when(operationDAO.updateEntity(subOp)).thenReturn(Mono.just(subOp));
        when(operationDAO.getByOperationId("parentId")).thenReturn(Mono.just(parent));
        when(operationDAO.getByOperationId("SUB#parentId#IUN-002")).thenReturn(Mono.just(sibling));
        when(operationDAO.updateEntity(parent)).thenReturn(Mono.just(parent));

        assertDoesNotThrow(() -> resultExternalChannelAction.execute(dto));

        ArgumentCaptor<PnServiceDeskOperations> captor = ArgumentCaptor.forClass(PnServiceDeskOperations.class);
        verify(operationDAO, atLeastOnce()).updateEntity(captor.capture());
        boolean parentUpdatedKo = captor.getAllValues().stream()
                .anyMatch(op -> "parentId".equals(op.getOperationId()) && OperationStatusEnum.KO.name().equals(op.getStatus()));
        assertTrue(parentUpdatedKo, "Parent operation should be updated to KO");
    }

    @Test
    void testExecute_subOp_notAllDone_doesNotUpdateParent() {
        String subOpId = "SUB#parentId#IUN-001";
        SingleStatusUpdateDto dto = getDto(subOpId, ProgressEventCategoryDto.OK, CourtesyMessageProgressEventDto.EventCodeEnum.M003);

        PnServiceDeskOperations subOp = new PnServiceDeskOperations();
        subOp.setOperationId(subOpId);
        subOp.setIsSubOperation(Boolean.TRUE);

        PnServiceDeskOperations sibling = new PnServiceDeskOperations();
        sibling.setOperationId("SUB#parentId#IUN-002");
        sibling.setStatus(OperationStatusEnum.PROGRESS.name());

        PnServiceDeskOperations parent = new PnServiceDeskOperations();
        parent.setOperationId("parentId");
        parent.setSubOperationsIds(List.of(subOpId, "SUB#parentId#IUN-002"));

        when(operationDAO.getByOperationId(subOpId)).thenReturn(Mono.just(subOp));
        when(operationDAO.updateEntity(subOp)).thenReturn(Mono.just(subOp));
        when(operationDAO.getByOperationId("parentId")).thenReturn(Mono.just(parent));
        when(operationDAO.getByOperationId("SUB#parentId#IUN-002")).thenReturn(Mono.just(sibling));

        assertDoesNotThrow(() -> resultExternalChannelAction.execute(dto));

        // Only the sub-op itself should be updated, not the parent
        verify(operationDAO, times(1)).updateEntity(subOp);
        verify(operationDAO, never()).updateEntity(parent);
    }

    @Test
    void testExecute_nonSubOp_doesNotUpdateParent() {
        SingleStatusUpdateDto dto = getDto("LEGACYOP", ProgressEventCategoryDto.OK, CourtesyMessageProgressEventDto.EventCodeEnum.M003);

        PnServiceDeskOperations legacyOp = new PnServiceDeskOperations();
        legacyOp.setOperationId("LEGACYOP");
        // isSubOperation is null → legacy operation

        when(operationDAO.getByOperationId("LEGACYOP")).thenReturn(Mono.just(legacyOp));
        when(operationDAO.updateEntity(legacyOp)).thenReturn(Mono.just(legacyOp));

        assertDoesNotThrow(() -> resultExternalChannelAction.execute(dto));

        verify(operationDAO, times(1)).updateEntity(legacyOp);
        // getByOperationId called only once (for the operation itself, not for any parent)
        verify(operationDAO, times(1)).getByOperationId("LEGACYOP");
    }
}
