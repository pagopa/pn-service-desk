package it.pagopa.pn.service.desk.action.impl;

import it.pagopa.pn.service.desk.action.ResultPaperChannelAction;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.SendEventDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.StatusCodeEnumDto;
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
import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ENTITY_NOT_FOUND;
import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.PAPERCHANNEL_STATUS_CODE_EMPTY;


@Component
@CustomLog
@AllArgsConstructor
public class ResultPaperChannelActionImpl implements ResultPaperChannelAction {

    private OperationDAO operationDAO;


    @Override
    public void execute(SendEventDto sendEventDto) {
        String operationId = Utility.extractOperationId(sendEventDto.getRequestId());
        operationDAO.getByOperationId(operationId)
            .switchIfEmpty(Mono.error(() -> {
                log.debug("The operation entity was not found with this operationId: {}", operationId);
                return new PnGenericException(ENTITY_NOT_FOUND, ENTITY_NOT_FOUND.getMessage());
            }))
            .flatMap(entityOperation -> {
                if(sendEventDto.getStatusCode() == null || StringUtils.isBlank(sendEventDto.getStatusCode().getValue())) {
                    log.debug("The status code is null or empty");
                    return Mono.error(new PnGenericException(PAPERCHANNEL_STATUS_CODE_EMPTY, PAPERCHANNEL_STATUS_CODE_EMPTY.getMessage()));
                }
                OperationStatusEnum newStatus = Utility.getOperationStatusFrom(sendEventDto.getStatusCode());
                return updateOperationEventAndStatus(entityOperation, newStatus, sendEventDto);
            })
            .onErrorResume(error -> {
                OperationStatusEnum newStatus = Utility.getOperationStatusFrom(StatusCodeEnumDto.KO);
                return operationDAO
                        .getByOperationId(operationId)
                        .flatMap(entityOperation ->
                                updateOperationEventAndStatus(entityOperation, newStatus, sendEventDto));
            })
            .block();
    }

    private Mono<Void> updateOperationEventAndStatus(PnServiceDeskOperations entityOperation, OperationStatusEnum operationStatusEnum, SendEventDto sendEventDto){
        log.debug("Update operation entity and event with new status: {}", operationStatusEnum.toString());
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
