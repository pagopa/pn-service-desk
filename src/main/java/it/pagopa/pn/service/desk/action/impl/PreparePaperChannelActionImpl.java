package it.pagopa.pn.service.desk.action.impl;

import it.pagopa.pn.service.desk.action.PreparePaperChannelAction;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.PrepareEventDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.StatusCodeEnumDto;
import it.pagopa.pn.service.desk.mapper.PaperChannelMapper;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.paperchannel.PnPaperChannelClient;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import it.pagopa.pn.service.desk.utility.Utility;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@Component
@AllArgsConstructor
public class PreparePaperChannelActionImpl implements PreparePaperChannelAction {

    private OperationDAO operationDAO;

    private PnPaperChannelClient paperChannelClient;

    private PnServiceDeskConfigs pnServiceDeskConfigs;

    @Override
    public void execute(PrepareEventDto eventDto) {
        String operationId = Utility.extractOperationId(eventDto.getRequestId());
        operationDAO.getByOperationId(operationId)
                .map(entityOperation -> {
                    if(eventDto.getStatusCode() != null && StringUtils.isNotBlank(eventDto.getStatusCode().getValue())
                            && StringUtils.equals(eventDto.getStatusCode().getValue(), StatusCodeEnumDto.KO.getValue())) {
                        entityOperation.setStatus(StatusCodeEnumDto.KO.getValue());
                        return updateOperationStatus(entityOperation, OperationStatusEnum.KO, eventDto.getStatusDetail());
                    } else {
                        return paperSendRequest(pnServiceDeskConfigs, entityOperation, eventDto);
                    }
                }).block();

    }

    private Mono<Void> paperSendRequest(PnServiceDeskConfigs pnServiceDeskConfigs, PnServiceDeskOperations entityOperation, PrepareEventDto prepareEventDto){
        String requestId = Utility.generateRequestId(entityOperation.getOperationId());
        return paperChannelClient.sendPaperSendRequest(requestId, PaperChannelMapper.getPaperSendRequest(pnServiceDeskConfigs, entityOperation, prepareEventDto))
                .flatMap(response -> updateOperationStatus(entityOperation, OperationStatusEnum.PROGRESS, ""))
                .onErrorResume(error -> updateOperationStatus(entityOperation, OperationStatusEnum.KO, error.getMessage()));
    }

    private Mono<Void> updateOperationStatus(PnServiceDeskOperations entityOperation, OperationStatusEnum operationStatusEnum, String errorReason){
        entityOperation.setStatus(operationStatusEnum.toString());
        if(StringUtils.isNotBlank(errorReason)) {
            entityOperation.setErrorReason(errorReason);
        }
        return this.operationDAO.updateEntity(entityOperation).then();
    }
}