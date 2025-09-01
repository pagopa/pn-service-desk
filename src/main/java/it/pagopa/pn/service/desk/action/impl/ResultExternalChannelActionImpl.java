package it.pagopa.pn.service.desk.action.impl;


import it.pagopa.pn.service.desk.action.ResultExternalChannelAction;
import it.pagopa.pn.service.desk.action.common.CommonAction;
import it.pagopa.pn.service.desk.exception.PnEntityNotFoundException;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.dto.CourtesyMessageProgressEventDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.dto.ProgressEventCategoryDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.dto.SingleStatusUpdateDto;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import it.pagopa.pn.service.desk.utility.Utility;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.EXTERNALCHANNEL_STATUS_CODE_EMPTY;


@Component
@CustomLog
@AllArgsConstructor
public class ResultExternalChannelActionImpl extends CommonAction implements ResultExternalChannelAction {

    private OperationDAO operationDAO;

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
                if (StringUtils.isBlank(courtesyMessageProgressEventDto.getStatus().getValue())) {
                    log.error("entityOperation = {}, operationId = {}, Status code is null or blank", entityOperation, operationId);
                    return Mono.error(new PnGenericException(EXTERNALCHANNEL_STATUS_CODE_EMPTY, EXTERNALCHANNEL_STATUS_CODE_EMPTY.getMessage()));
                }
                OperationStatusEnum newStatus = Utility.getEcOperationStatusFrom(courtesyMessageProgressEventDto.getStatus());
                entityOperation.setStatus(newStatus.name());
                if ( courtesyMessageProgressEventDto.getStatus().equals(ProgressEventCategoryDto.OK)){
                log.info("entityOperation = {}, operationId = {}, Status code is OK, updating operation status to {}", entityOperation, operationId, newStatus);
                } else {
                    log.warn("entityOperation = {}, operationId = {}, Status code is not OK, updating operation status to {}", entityOperation, operationId, newStatus);
                }
                return operationDAO.updateEntity(entityOperation).then();

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

}
