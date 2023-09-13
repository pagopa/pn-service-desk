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
        log.debug("prepareEventDto = {}, PreparePaperChannelAction - Execute received input", prepareEventDto);

        log.debug("operationId = {}, Retrieving entityOperation from Database", operationId);
        operationDAO.getByOperationId(operationId)
            .switchIfEmpty(Mono.error(new PnEntityNotFoundException()))
            .flatMap(entityOperation -> {
                log.debug("operationId = {}, entityOperation = {}, Is Status code null or blank?", operationId, entityOperation);
                if(prepareEventDto.getStatusCode() == null || StringUtils.isBlank(prepareEventDto.getStatusCode().getValue())) {
                    log.error("operationId = {}, entityOperation = {}, Status code is null or blank", operationId, entityOperation);
                    return Mono.error(new PnGenericException(PAPERCHANNEL_STATUS_CODE_EMPTY, PAPERCHANNEL_STATUS_CODE_EMPTY.getMessage()));
                }
                log.debug("operationId = {}, entityOperation = {}, statusCode = {}, Is Status code in PROGRESS?", operationId, entityOperation, prepareEventDto.getStatusCode().getValue());
                if(prepareEventDto.getStatusCode() == StatusCodeEnumDto.OK) {
                    log.debug("operationId = {}, entityOperation = {}, statusCode = {}, Status code is in PROGRESS", operationId, entityOperation, prepareEventDto.getStatusCode().getValue());
                    return paperSendRequest(pnServiceDeskConfigs, entityOperation, prepareEventDto);
                }
                log.debug("operationId = {}, entityOperation = {}, statusCode = {}, Status code is different from PROGRESS", operationId, entityOperation, prepareEventDto.getStatusCode().getValue());
                return Mono.just(entityOperation);
            })
            .flatMap(entityOperation -> {
                OperationStatusEnum newStatus = OperationStatusEnum.PROGRESS;
                log.debug("operationId = {}, requestId = {}, entityOperation = {}, operationStatus = {}, Is prepareEvent'status code different from OK?", operationId, prepareEventDto.getRequestId(), entityOperation, prepareEventDto.getStatusCode());
                if (prepareEventDto.getStatusCode() != StatusCodeEnumDto.OK) {
                    log.debug("operationId = {}, requestId = {}, entityOperation = {}, operationStatus = {}, PrepareEvent'status code is different from OK", operationId, prepareEventDto.getRequestId(), entityOperation, newStatus.toString());
                    newStatus = Utility.getOperationStatusFrom(prepareEventDto.getStatusCode());
                }
                log.debug("operationId = {}, requestId = {}, entityOperation = {}, operationStatus = {}, Paper send request has been sent", operationId, prepareEventDto.getRequestId(), entityOperation, newStatus.toString());
                return updateOperationStatus(prepareEventDto, entityOperation, newStatus, null);
            })
            .doOnError(PnEntityNotFoundException.class, error -> log.error("operationId = {}, statusCode = {}, Operation entity was not found", operationId, prepareEventDto.getStatusCode().getValue()))
            .onErrorResume(exception -> {
                if(exception instanceof PnEntityNotFoundException) {
                    return Mono.empty();
                }
                OperationStatusEnum newStatus = Utility.getOperationStatusFrom(StatusCodeEnumDto.KO);
                log.error("operationId = {}, operationStatus = {}, errorReason = {}, Paper send request has gone on error", operationId, newStatus, exception.getMessage());
                return operationDAO
                        .getByOperationId(operationId)
                        .flatMap(entityOperation ->
                                updateOperationStatus(null, entityOperation, newStatus, exception.getMessage()));
            })
            .block();
    }

    private Mono<PnServiceDeskOperations> paperSendRequest(PnServiceDeskConfigs pnServiceDeskConfigs, PnServiceDeskOperations entityOperation, PrepareEventDto prepareEventDto) {
        String requestId = prepareEventDto.getRequestId();
        log.debug("operationId = {}, requestId = {}, statusCode = {}, PaperSendRequest received input", entityOperation.getOperationId(), requestId, prepareEventDto.getStatusCode().getValue());

        log.debug("operationId = {}, recipientInternalId = {}, Executing deanonymization of fiscal code", entityOperation.getOperationId(), entityOperation.getRecipientInternalId());
        return this.pnDataVaultClient.deAnonymized(entityOperation.getRecipientInternalId())
                        .map(fiscalCode -> {
                            log.debug("operationId = {}, requestId = {}, statusCode = {}, Building paper send request", entityOperation.getOperationId(), requestId, prepareEventDto.getStatusCode().getValue());
                            return PaperChannelMapper.getPaperSendRequest(pnServiceDeskConfigs, entityOperation, prepareEventDto, fiscalCode);
                        })
                        .flatMap(sendRequestDto -> {
                            log.debug("operationId = {}, requestId = {}, statusCode = {}, Executing paper send client request", entityOperation.getOperationId(), requestId, prepareEventDto.getStatusCode().getValue());
                            return paperChannelClient.sendPaperSendRequest(requestId, sendRequestDto);
                        })
                        .thenReturn(entityOperation);
    }

    private Mono<Void> updateOperationStatus(PrepareEventDto prepareEventDto,
                                             @NotNull PnServiceDeskOperations entityOperation,
                                             @NotNull OperationStatusEnum operationStatusEnum,
                                             String errorReason){
        log.debug("operationId = {}, operationStatus = {}, erroReason = {}, UpdateOperationStatus received input", entityOperation.getOperationId(), operationStatusEnum, errorReason);

        log.debug("operationId = {}, operationStatus = {}, Is prepareEventDto null?", entityOperation.getOperationId(), operationStatusEnum);
        if(prepareEventDto != null) {
            String operationId = Utility.extractOperationId(prepareEventDto.getRequestId());
            log.debug("operationId = {}, requestId = {}, operationStatus = {}, PrepareEventDto is not null", operationId, prepareEventDto.getRequestId(), operationStatusEnum);
            PnServiceDeskEvents pnServiceDeskEvents = new PnServiceDeskEvents();
            pnServiceDeskEvents.setStatusCode(prepareEventDto.getStatusDetail());
            pnServiceDeskEvents.setStatusDescription(prepareEventDto.getStatusCode().getValue().concat(" - ").concat(prepareEventDto.getStatusDetail()));

            log.debug("operationId = {}, requestId = {}, operationStatus = {}, Is entityOperation's list's events not null?", operationId, prepareEventDto.getRequestId(), operationStatusEnum);
            if(entityOperation.getEvents() == null) {
                log.debug("operationId = {}, requestId = {}, operationStatus = {}, A new entityOperation's list's events was created and new event has been added", operationId, prepareEventDto.getRequestId(), operationStatusEnum);
                List<PnServiceDeskEvents> eventsList = new ArrayList<>();
                entityOperation.setEvents(eventsList);
            }
            log.debug("operationId = {}, requestId = {}, operationStatus = {}, EntityOperation's list's events was not null and new event has been added", operationId, prepareEventDto.getRequestId(), operationStatusEnum);
            entityOperation.getEvents().add(pnServiceDeskEvents);
        }
        entityOperation.setErrorReason(errorReason);
        entityOperation.setStatus(operationStatusEnum.toString());

        log.debug("operationId = {}, operationStatus = {}, Update entityOperation and event with new status", entityOperation.getOperationId(), operationStatusEnum);
        return this.operationDAO.updateEntity(entityOperation).then();
    }
}