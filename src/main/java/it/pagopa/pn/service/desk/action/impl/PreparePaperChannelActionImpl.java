package it.pagopa.pn.service.desk.action.impl;

import it.pagopa.pn.service.desk.action.PreparePaperChannelAction;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.exception.PnEntityNotFoundException;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.PrepareEventDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.StatusCodeEnumDto;
import it.pagopa.pn.service.desk.mapper.PaperChannelMapper;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskEvents;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault.PnDataVaultClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.paperchannel.PnPaperChannelClient;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import it.pagopa.pn.service.desk.utility.Utility;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.PAPERCHANNEL_STATUS_CODE_EMPTY;


@Component
@CustomLog
@AllArgsConstructor
public class PreparePaperChannelActionImpl implements PreparePaperChannelAction {

    private OperationDAO operationDAO;

    private PnPaperChannelClient paperChannelClient;

    private PnServiceDeskConfigs pnServiceDeskConfigs;
    private PnDataVaultClient pnDataVaultClient;


    @Override
    public void execute(PrepareEventDto prepareEventDto) {
        String operationId = Utility.extractOperationId(prepareEventDto.getRequestId());
        operationDAO.getByOperationId(operationId)
            .switchIfEmpty(Mono.error(new PnEntityNotFoundException()))
            .flatMap(entityOperation -> {
                if(prepareEventDto.getStatusCode() == null || StringUtils.isBlank(prepareEventDto.getStatusCode().getValue())) {
                    log.error("The status code is null or empty");
                    return Mono.error(new PnGenericException(PAPERCHANNEL_STATUS_CODE_EMPTY, PAPERCHANNEL_STATUS_CODE_EMPTY.getMessage()));
                }
                if(prepareEventDto.getStatusCode() == StatusCodeEnumDto.PROGRESS) {
                    return paperSendRequest(pnServiceDeskConfigs, entityOperation, prepareEventDto);
                }
                return Mono.just(entityOperation);
            })
            .flatMap(entityOperation -> {
                OperationStatusEnum newStatus = Utility.getOperationStatusFrom(prepareEventDto.getStatusCode());
                return updateOperationStatus(prepareEventDto, entityOperation, newStatus, null);
            })
            .doOnError(PnEntityNotFoundException.class, error -> log.error("The operation entity was not found with this operationId: {}", operationId))
            .onErrorResume(error -> {
                if(error instanceof PnEntityNotFoundException) {
                    return Mono.empty();
                }
                log.error("The operation paper send was gone on error: {}", error.getMessage());
                OperationStatusEnum newStatus = Utility.getOperationStatusFrom(StatusCodeEnumDto.KO);
                return operationDAO
                        .getByOperationId(operationId)
                        .flatMap(entityOperation ->
                                updateOperationStatus(null, entityOperation, newStatus, error.getMessage()));
            })
            .block();
    }

    private Mono<PnServiceDeskOperations> paperSendRequest(PnServiceDeskConfigs pnServiceDeskConfigs, PnServiceDeskOperations entityOperation, PrepareEventDto prepareEventDto) {
        String requestId = Utility.generateRequestId(entityOperation.getOperationId());
        log.debug("Executing paperchannel send with requestId: {}", requestId);
        return this.pnDataVaultClient.deAnonymized(entityOperation.getRecipientInternalId())
                        .map(fiscalCode -> PaperChannelMapper.getPaperSendRequest(pnServiceDeskConfigs, entityOperation, prepareEventDto, fiscalCode))
                        .flatMap(sendRequestDto -> paperChannelClient.sendPaperSendRequest(requestId, sendRequestDto))
                        .thenReturn(entityOperation);
    }

    private Mono<Void> updateOperationStatus(PrepareEventDto prepareEventDto,
                                             @NotNull PnServiceDeskOperations entityOperation,
                                             @NotNull OperationStatusEnum operationStatusEnum,
                                             String errorReason){
        log.debug("Update operation entity and event with new status: {}", operationStatusEnum);
        if(prepareEventDto != null) {
            PnServiceDeskEvents pnServiceDeskEvents = new PnServiceDeskEvents();
            pnServiceDeskEvents.setStatusCode(prepareEventDto.getStatusDetail());
            pnServiceDeskEvents.setStatusDescription(prepareEventDto.getStatusCode().getValue().concat(" - ").concat(prepareEventDto.getStatusDetail()));
            if(entityOperation.getEvents() == null) {
                List<PnServiceDeskEvents> eventsList = new ArrayList<>();
                entityOperation.setEvents(eventsList);
            }
            entityOperation.getEvents().add(pnServiceDeskEvents);
        }
        entityOperation.setErrorReason(errorReason);
        entityOperation.setStatus(operationStatusEnum.toString());

        return this.operationDAO.updateEntity(entityOperation).then();
    }

}