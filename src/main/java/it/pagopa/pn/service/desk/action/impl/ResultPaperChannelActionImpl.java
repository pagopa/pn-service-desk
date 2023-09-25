package it.pagopa.pn.service.desk.action.impl;

import it.pagopa.pn.service.desk.action.ResultPaperChannelAction;
import it.pagopa.pn.service.desk.action.common.CommonAction;
import it.pagopa.pn.service.desk.exception.PnEntityNotFoundException;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.SendEventDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.StatusCodeEnumDto;
import it.pagopa.pn.service.desk.mapper.ServiceDeskEventsMapper;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskEvents;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.queue.producer.InternalQueueMomProducer;
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
public class ResultPaperChannelActionImpl extends CommonAction implements ResultPaperChannelAction {

    private OperationDAO operationDAO;
    private InternalQueueMomProducer internalQueueMomProducer;

    @Override
    public void execute(SendEventDto sendEventDto) {
        String operationId = Utility.extractOperationId(sendEventDto.getRequestId());
        log.debug("sendEventDto = {}, ResultPaperChannelAction - Execute received input", sendEventDto);

        operationDAO.getByOperationId(operationId)
                .switchIfEmpty(Mono.error(new PnEntityNotFoundException()))
                .flatMap(entityOperation -> {
                    log.debug("entityOperation = {}, operationId = {}, Is sendEventDto null or blank?", entityOperation, operationId);
                    if(sendEventDto.getStatusCode() == null || StringUtils.isBlank(sendEventDto.getStatusCode().getValue())) {
                        log.error("entityOperation = {}, operationId = {}, Status code is null or blank", entityOperation, operationId);
                        return Mono.error(new PnGenericException(PAPERCHANNEL_STATUS_CODE_EMPTY, PAPERCHANNEL_STATUS_CODE_EMPTY.getMessage()));
                    }
                    OperationStatusEnum newStatus = Utility.getOperationStatusFrom(sendEventDto.getStatusCode());
                    if (sendEventDto.getStatusCode().equals(StatusCodeEnumDto.OK)){
                        return updateNotificationViewedAsync(entityOperation)
                                .flatMap(operationStatusEnum -> updateOperationEventAndStatus(sendEventDto, entityOperation, operationStatusEnum, null));
                    }
                    return updateOperationEventAndStatus(sendEventDto, entityOperation, newStatus, null);
                })
                .doOnError(PnEntityNotFoundException.class, error -> log.error("operationId = {}, EntityOperation was not found", operationId))
                .onErrorResume(exception -> {
                    if(exception instanceof PnEntityNotFoundException) {
                        return Mono.empty();
                    }
                    OperationStatusEnum newStatus = Utility.getOperationStatusFrom(StatusCodeEnumDto.KO);
                    return operationDAO
                            .getByOperationId(operationId)
                            .flatMap(entityOperation ->
                                    updateOperationEventAndStatus(null, entityOperation, newStatus, exception.getMessage()));
                })
                .block();
    }

    private Mono<Void> updateOperationEventAndStatus(SendEventDto sendEventDto,
                                                     @NotNull PnServiceDeskOperations entityOperation,
                                                     @NotNull OperationStatusEnum operationStatusEnum,
                                                     String errorReason){
        log.debug("operationId = {}, operationStatus = {}, erroReason = {}, updateOperationEventAndStatus received input", entityOperation.getOperationId(), operationStatusEnum, errorReason);

        log.debug("operationId = {}, operationStatus = {}, Is sendEventDto null?", entityOperation.getOperationId(), operationStatusEnum);
        if(sendEventDto != null) {
            String operationId = Utility.extractOperationId(sendEventDto.getRequestId());
            log.debug("operationId = {}, requestId = {}, operationStatus = {}, SendEventDto is not null", operationId, sendEventDto.getRequestId(), operationStatusEnum);
            PnServiceDeskEvents pnServiceDeskEvents = ServiceDeskEventsMapper.toEntity(sendEventDto.getStatusDetail(), sendEventDto.getStatusDescription());
            entityOperation.setOperationLastUpdateDate(pnServiceDeskEvents.getTimestamp());

            log.debug("operationId = {}, requestId = {}, operationStatus = {}, Is entityOperation's list's events not null?", operationId, sendEventDto.getRequestId(), operationStatusEnum);
            if(entityOperation.getEvents() == null) {
                log.debug("operationId = {}, requestId = {}, operationStatus = {}, A new list's events was created and new event has been added", operationId, sendEventDto.getRequestId(), operationStatusEnum);
                List<PnServiceDeskEvents> eventsList = new ArrayList<>();
                entityOperation.setEvents(eventsList);
            }
            log.debug("operationId = {}, requestId = {}, operationStatus = {}, EntityOperation's list's events was not null and new event has been added", operationId, sendEventDto.getRequestId(), operationStatusEnum);
            entityOperation.getEvents().add(pnServiceDeskEvents);
        }
        entityOperation.setErrorReason(errorReason);
        entityOperation.setStatus(operationStatusEnum.toString());

        log.debug("operationId = {}, operationStatus = {}, Update entityOperation and event with new status", entityOperation.getOperationId(), operationStatusEnum);
        return this.operationDAO.updateEntity(entityOperation).then();
    }

    private Mono<OperationStatusEnum> updateNotificationViewedAsync(PnServiceDeskOperations pnServiceDeskOperations) {
        log.info("call notificationViewed for operationId {}", pnServiceDeskOperations.getOperationId());

        if (pnServiceDeskOperations.getAttachments() != null && !pnServiceDeskOperations.getAttachments().isEmpty()) {
            return pushNotificationViewedMessage(pnServiceDeskOperations.getAttachments().stream()
                    .filter(attachments -> attachments.getIsAvailable() == Boolean.TRUE && StringUtils.isNotBlank(attachments.getIun()))
                    .map(PnServiceDeskAttachments::getIun)
                    .toList(), pnServiceDeskOperations);
        }
        return Mono.just(OperationStatusEnum.OK);
    }

    private Mono<OperationStatusEnum> pushNotificationViewedMessage(List<String> iuns, PnServiceDeskOperations pnServiceDeskOperations){
        if (iuns == null || iuns.isEmpty()) {
            return Mono.just(OperationStatusEnum.OK);
        }

        log.info("push message on queue for operationId {}", pnServiceDeskOperations.getOperationId());
        internalQueueMomProducer.push(getInternalEvent(iuns, pnServiceDeskOperations.getOperationId(), pnServiceDeskOperations.getRecipientInternalId()));
        return Mono.just(OperationStatusEnum.NOTIFY_VIEW);
    }

}
