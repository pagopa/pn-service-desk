package it.pagopa.pn.service.desk.action.impl;

import it.pagopa.pn.service.desk.action.ResultPaperChannelAction;
import it.pagopa.pn.service.desk.exception.PnEntityNotFoundException;
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
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

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
            .switchIfEmpty(Mono.error(new PnEntityNotFoundException()))
            .flatMap(entityOperation -> {
                if(sendEventDto.getStatusCode() == null || StringUtils.isBlank(sendEventDto.getStatusCode().getValue())) {
                    log.error("The status code is null or empty");
                    return Mono.error(new PnGenericException(PAPERCHANNEL_STATUS_CODE_EMPTY, PAPERCHANNEL_STATUS_CODE_EMPTY.getMessage()));
                }
                OperationStatusEnum newStatus = Utility.getOperationStatusFrom(sendEventDto.getStatusCode());
                return updateOperationEventAndStatus(sendEventDto, entityOperation, newStatus, null);
            })
            .doOnError(PnEntityNotFoundException.class, error -> log.error("The operation entity was not found with this operationId: {}", operationId))
            .onErrorResume(error -> {
                if(error instanceof PnEntityNotFoundException) {
                    return Mono.empty();
                }
                OperationStatusEnum newStatus = Utility.getOperationStatusFrom(StatusCodeEnumDto.KO);
                return operationDAO
                    .getByOperationId(operationId)
                    .flatMap(entityOperation ->
                            updateOperationEventAndStatus(null, entityOperation, newStatus, error.getMessage()));
            })
            .block();
    }

    private Mono<Void> updateOperationEventAndStatus(SendEventDto sendEventDto,
                                                     @NotNull PnServiceDeskOperations entityOperation,
                                                     @NotNull OperationStatusEnum operationStatusEnum,
                                                     String errorReason){
        log.debug("Update operation entity and event with new status: {}", operationStatusEnum.toString());
        if(sendEventDto != null) {
            PnServiceDeskEvents pnServiceDeskEvents = new PnServiceDeskEvents();
            pnServiceDeskEvents.setStatusCode(sendEventDto.getStatusDetail());
            pnServiceDeskEvents.setStatusDescription(sendEventDto.getStatusDescription());
            entityOperation.getEvents().add(pnServiceDeskEvents);
        }
        entityOperation.setErrorReason(errorReason);

        entityOperation.setStatus(operationStatusEnum.toString());

        return this.operationDAO.updateEntity(entityOperation).then();
    }

}
