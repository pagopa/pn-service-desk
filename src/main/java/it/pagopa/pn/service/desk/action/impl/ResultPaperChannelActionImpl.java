package it.pagopa.pn.service.desk.action.impl;

import it.pagopa.pn.service.desk.action.ResultPaperChannelAction;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.SendEventDto;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskEvents;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import it.pagopa.pn.service.desk.utility.Utility;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@CustomLog
@AllArgsConstructor
public class ResultPaperChannelActionImpl implements ResultPaperChannelAction {

    private OperationDAO operationDAO;


    @Override
    public void execute(SendEventDto sendEventDto) {
        String operationId = Utility.extractOperationId(sendEventDto.getRequestId());
        operationDAO.getByOperationId(operationId)
                .flatMap(entityOperation -> {
                    if(sendEventDto.getStatusCode() != null && !StringUtils.isNotBlank(sendEventDto.getStatusCode().getValue())) {
                        OperationStatusEnum newStatus = Utility.getOperationStatusFrom(sendEventDto.getStatusCode());
                        return updateOperationEventAndStatus(entityOperation, newStatus, sendEventDto);
                    }
                   //TODO status code empty
                    return null;
                }).block();
    }



    private Mono<Void> updateOperationEventAndStatus(PnServiceDeskOperations entityOperation, OperationStatusEnum operationStatusEnum, SendEventDto sendEventDto){
        //UPDATE EVENT
        PnServiceDeskEvents pnServiceDeskEvents = new PnServiceDeskEvents();
        pnServiceDeskEvents.setStatusCode(sendEventDto.getStatusDetail());
        pnServiceDeskEvents.setStatusDescription(sendEventDto.getStatusDescription());
        entityOperation.getEvents().add(pnServiceDeskEvents);
        //UPDATE STATUS
        entityOperation.setStatus(operationStatusEnum.toString());
        entityOperation.setErrorReason(sendEventDto.getDeliveryFailureCause());
        return this.operationDAO.updateEntity(entityOperation).then();
    }
}
