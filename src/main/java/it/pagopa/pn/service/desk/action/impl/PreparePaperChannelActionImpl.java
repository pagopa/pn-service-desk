package it.pagopa.pn.service.desk.action.impl;

import it.pagopa.pn.service.desk.action.PreparePaperChannelAction;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.PrepareEventDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.StatusCodeEnumDto;
import it.pagopa.pn.service.desk.mapper.PaperChannelMapper;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.paperchannel.PnPaperChannelClient;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import it.pagopa.pn.service.desk.utility.Utility;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ENTITY_NOT_FOUND;
import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.PAPERCHANNEL_STATUS_CODE_EMPTY;


@Component
@CustomLog
@AllArgsConstructor
public class PreparePaperChannelActionImpl implements PreparePaperChannelAction {

    private OperationDAO operationDAO;

    private PnPaperChannelClient paperChannelClient;

    private PnServiceDeskConfigs pnServiceDeskConfigs;


    @Override
    public void execute(PrepareEventDto eventDto) {
        String operationId = Utility.extractOperationId(eventDto.getRequestId());
        operationDAO.getByOperationId(operationId)
                .switchIfEmpty(Mono.error(() -> {
                    log.debug("The operation entity was not found with this operationId: {}", operationId);
                    return new PnGenericException(ENTITY_NOT_FOUND, ENTITY_NOT_FOUND.getMessage());
                }))            .flatMap(entityOperation -> {
                if(eventDto.getStatusCode() == null || StringUtils.isBlank(eventDto.getStatusCode().getValue())) {
                    log.debug("The status code is null or empty");
                    return Mono.error(new PnGenericException(PAPERCHANNEL_STATUS_CODE_EMPTY, PAPERCHANNEL_STATUS_CODE_EMPTY.getMessage()));
                }
                if(eventDto.getStatusCode() == StatusCodeEnumDto.PROGRESS) {
                    return paperSendRequest(pnServiceDeskConfigs, entityOperation, eventDto);
                }
                OperationStatusEnum newStatus = Utility.getOperationStatusFrom(eventDto.getStatusCode());
                return updateOperationStatus(entityOperation, newStatus, eventDto.getStatusDetail());
            })
            .onErrorResume(error -> {
                OperationStatusEnum newStatus = Utility.getOperationStatusFrom(StatusCodeEnumDto.KO);
                return operationDAO
                        .getByOperationId(operationId)
                        .flatMap(entityOperation ->
                                updateOperationStatus(entityOperation, newStatus, error.getMessage()));
            })
            .block();
    }

    private Mono<Void> paperSendRequest(PnServiceDeskConfigs pnServiceDeskConfigs, PnServiceDeskOperations entityOperation, PrepareEventDto prepareEventDto){
        String requestId = Utility.generateRequestId(entityOperation.getOperationId());
        log.debug("Executing Paperchannel Send with requestId: {}", requestId);
        return paperChannelClient.sendPaperSendRequest(requestId, PaperChannelMapper.getPaperSendRequest(pnServiceDeskConfigs, entityOperation, prepareEventDto))
                .flatMap(response -> updateOperationStatus(entityOperation, OperationStatusEnum.PROGRESS, ""))
                .onErrorResume(error -> updateOperationStatus(entityOperation, OperationStatusEnum.KO, error.getMessage()));
    }

    private Mono<Void> updateOperationStatus(PnServiceDeskOperations entityOperation, OperationStatusEnum operationStatusEnum, String errorReason){
        log.debug("Update operation entity and event with new status: {}", operationStatusEnum.toString());
        entityOperation.setStatus(operationStatusEnum.toString());
        if(StringUtils.isNotBlank(errorReason)) {
            entityOperation.setErrorReason(errorReason);
        }
        return this.operationDAO.updateEntity(entityOperation).then();
    }
}