package it.pagopa.pn.service.desk.action.impl;


import it.pagopa.pn.service.desk.action.ResultExternalChannelAction;
import it.pagopa.pn.service.desk.action.common.CommonAction;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.exception.PnEntityNotFoundException;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.dto.CourtesyMessageProgressEventDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.dto.SingleStatusUpdateDto;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import it.pagopa.pn.service.desk.utility.Utility;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.EXTERNALCHANNEL_STATUS_CODE_EMPTY;


@Component
@CustomLog
@AllArgsConstructor
public class ResultExternalChannelActionImpl extends CommonAction implements ResultExternalChannelAction {

    private OperationDAO operationDAO;
    private PnServiceDeskConfigs cfg;

    @Override
    public void execute(SingleStatusUpdateDto singleStatusUpdateDto) {
        log.debug("singleStatusUpdate = {}, ResultExternalChannelAction - Execute received input", singleStatusUpdateDto);
        CourtesyMessageProgressEventDto courtesyMessageProgressEventDto = singleStatusUpdateDto.getDigitalCourtesy();

        if (courtesyMessageProgressEventDto == null) {
            log.error("Received null CourtesyMessageProgressEventDto in SingleStatusUpdateDto: {}", singleStatusUpdateDto);
            throw new PnGenericException(EXTERNALCHANNEL_STATUS_CODE_EMPTY, EXTERNALCHANNEL_STATUS_CODE_EMPTY.getMessage());
        }

        String operationId = Utility.extractOperationId(courtesyMessageProgressEventDto.getRequestId());
        operationDAO.getByOperationId(operationId)
            .switchIfEmpty(Mono.error(new PnEntityNotFoundException()))
            .flatMap(entityOperation -> {
                if (StringUtils.isBlank(courtesyMessageProgressEventDto.getEventCode().getValue())) {
                    log.error("entityOperation = {}, operationId = {}, Status code is null or blank", entityOperation, operationId);
                    return Mono.error(new PnGenericException(EXTERNALCHANNEL_STATUS_CODE_EMPTY, EXTERNALCHANNEL_STATUS_CODE_EMPTY.getMessage()));
                }
                OperationStatusEnum newStatus = convertExternalChannelStatusToOperationStatus(courtesyMessageProgressEventDto.getEventCode());
                entityOperation.setStatus(newStatus.name());
                if ( newStatus.equals(OperationStatusEnum.OK) ) {
                log.info("entityOperation = {}, operationId = {}, Status code is OK, updating operation status to {}", entityOperation, operationId, newStatus);
                } else {
                    log.warn("entityOperation = {}, operationId = {}, Status code is not OK, updating operation status to {}", entityOperation, operationId, newStatus);
                }
                return operationDAO.updateEntity(entityOperation)
                        .then(updateParentAggregateStatus(entityOperation));

            })
                .doOnError( error -> log.error("Error while processing courtesyMessageProgressEventDto = {}, operationId = {}, error = {}", courtesyMessageProgressEventDto, operationId, error.getMessage()))
                    .onErrorResume( error -> {
                    if (error instanceof PnEntityNotFoundException) {
                        log.error("Operation with id {} not found", operationId);
                        return Mono.empty();
                    } else if (error instanceof PnGenericException) {
                        log.error("Generic error occurred: {}", error.getMessage());
                        return Mono.empty();
                    } else {
                        log.error("Unexpected error occurred: {}", error.getMessage());
                        return Mono.empty();
                    }
                }).block();
    }

    private OperationStatusEnum convertExternalChannelStatusToOperationStatus(CourtesyMessageProgressEventDto.EventCodeEnum status) {
        if (cfg.getExternalChannelDigitalCodesSuccess().contains(status.getValue()))
            return OperationStatusEnum.OK;
        if (cfg.getExternalChannelDigitalCodesFailure().contains(status.getValue()))
            return OperationStatusEnum.KO;
        return OperationStatusEnum.PROGRESS;
    }

    private Mono<Void> updateParentAggregateStatus(PnServiceDeskOperations subOp) {
        if (!Boolean.TRUE.equals(subOp.getIsSubOperation())) return Mono.empty();
        String parentOperationId = Utility.resolveAddressOperationId(subOp.getOperationId());
        return operationDAO.getByOperationId(parentOperationId)
                .flatMap(parent -> {
                    if (parent.getSubOperationsIds() == null || parent.getSubOperationsIds().isEmpty()) return Mono.empty();
                    return Flux.fromIterable(parent.getSubOperationsIds())
                            .flatMap(operationDAO::getByOperationId)
                            .collectList()
                            .flatMap(allSubOps -> {
                                OperationStatusEnum aggregate = computeAggregateStatus(allSubOps);
                                if (aggregate == null) return Mono.empty();
                                parent.setStatus(aggregate.name());
                                parent.setOperationLastUpdateDate(Instant.now());
                                return operationDAO.updateEntity(parent).then();
                            });
                });
    }

    private OperationStatusEnum computeAggregateStatus(List<PnServiceDeskOperations> subOps) {
        Set<String> finalStatuses = Set.of(OperationStatusEnum.OK.name(), OperationStatusEnum.KO.name());
        if (!subOps.stream().allMatch(op -> finalStatuses.contains(op.getStatus()))) return null;
        boolean anyOk = subOps.stream().anyMatch(op -> OperationStatusEnum.OK.name().equals(op.getStatus()));
        boolean anyKo = subOps.stream().anyMatch(op -> OperationStatusEnum.KO.name().equals(op.getStatus()));
        if (anyOk && anyKo) return OperationStatusEnum.WARNING;
        return anyOk ? OperationStatusEnum.OK : OperationStatusEnum.KO;
    }

}
