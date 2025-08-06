package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskEvents;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import it.pagopa.pn.service.desk.utility.Utility;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class OperationMapper {

    private OperationMapper(){}

    public static PnServiceDeskOperations getInitialOperation(CreateOperationRequest req, String recipientInternalId) {
        return buildInitialOperation(req.getTicketId(), req.getTicketOperationId(), recipientInternalId);
    }

    public static PnServiceDeskOperations getInitialActOperation(CreateActOperationRequest req, String recipientInternalId) {
        return buildInitialOperation(req.getTicketId(), req.getTicketOperationId(), recipientInternalId);
    }

    private static PnServiceDeskOperations buildInitialOperation(String ticketId, String ticketOperationId, String recipientInternalId) {
        PnServiceDeskOperations pnServiceDeskOperations = new PnServiceDeskOperations();
        pnServiceDeskOperations.setOperationId(Utility.generateOperationId(ticketId, ticketOperationId));
        pnServiceDeskOperations.setTicketId(ticketId);
        pnServiceDeskOperations.setStatus(OperationStatusEnum.CREATING.toString());
        pnServiceDeskOperations.setOperationStartDate(Instant.now());
        pnServiceDeskOperations.setOperationLastUpdateDate(Instant.now());
        pnServiceDeskOperations.setRecipientInternalId(recipientInternalId);
        return pnServiceDeskOperations;
    }
    public static OperationResponse operationResponseMapper(PnServiceDeskConfigs pnServiceDeskConfigs, PnServiceDeskOperations pnServiceDeskOperations, String taxId){
        OperationResponse operationResponse = new OperationResponse();
        operationResponse.setOperationId(Utility.cleanUpOperationId(pnServiceDeskOperations.getOperationId()));
        List<SDNotificationSummary> iunsList = new ArrayList<>();
        List<SDNotificationSummary> uncompletedIunsList = new ArrayList<>();
        operationResponse.setIuns(iunsList);
        operationResponse.setUncompletedIuns(uncompletedIunsList);

        List<PnServiceDeskAttachments> attachments = pnServiceDeskOperations.getAttachments();
        if (attachments != null) {
            attachments.forEach(att -> {
                SDNotificationSummary summary = new SDNotificationSummary();
                summary.setIun(att.getIun());
                summary.setSenderPaInternalId(pnServiceDeskConfigs.getSenderPaId());
                summary.setSenderPaIpaCode(pnServiceDeskConfigs.getSenderIpaCode());
                summary.setSenderPaTaxCode(pnServiceDeskConfigs.getSenderTaxCode());
                summary.setSenderPaDescription(pnServiceDeskConfigs.getSenderAddress().getFullname());
                if (Boolean.TRUE.equals(att.getIsAvailable())) {
                    operationResponse.getIuns().add(summary);
                } else {
                    operationResponse.getUncompletedIuns().add(summary);
                }
            });
        }
        operationResponse.setOperationCreateTimestamp(OffsetDateTime.ofInstant(pnServiceDeskOperations.getOperationStartDate(), ZoneOffset.UTC));
        if (pnServiceDeskOperations.getOperationLastUpdateDate() != null) {
            operationResponse.setOperationUpdateTimestamp( OffsetDateTime.ofInstant(pnServiceDeskOperations.getOperationLastUpdateDate(), ZoneOffset.UTC));
        }
        NotificationStatus status = new NotificationStatus();
        if (pnServiceDeskOperations.getStatus().equals(OperationStatusEnum.NOTIFY_VIEW.toString())
                || pnServiceDeskOperations.getStatus().equals(OperationStatusEnum.NOTIFY_VIEW_ERROR.toString())) {
            pnServiceDeskOperations.setStatus(OperationStatusEnum.OK.toString());
        }
        status.setStatus(NotificationStatus.StatusEnum.fromValue(pnServiceDeskOperations.getStatus()));

        if (pnServiceDeskOperations.getEvents() != null && !pnServiceDeskOperations.getEvents().isEmpty()) {
            PnServiceDeskEvents e = pnServiceDeskOperations.getEvents().stream()
                    .filter(events -> events.getTimestamp() != null)
                    .max(Comparator.comparing(PnServiceDeskEvents::getTimestamp))
                    .orElse(new PnServiceDeskEvents());
            status.setStatusCode(e.getStatusCode());
            status.setStatusDescription(e.getStatusDescription());
            if (e.getTimestamp() != null) status.setLastEventTimestamp(Utility.getOffsetDateTimeFromDate(e.getTimestamp()));
        }

        if (StringUtils.isNotEmpty(pnServiceDeskOperations.getErrorReason())) {
            status.setStatusDescription(pnServiceDeskOperations.getErrorReason());
        }
        operationResponse.setNotificationStatus(status);
        operationResponse.setTaxId(taxId);

        return operationResponse;
    }

    public static PnServiceDeskOperations copyOperation (PnServiceDeskOperations operations){
        PnServiceDeskOperations pnServiceDeskOperations = new PnServiceDeskOperations();
        BeanUtils.copyProperties(operations, pnServiceDeskOperations);
        return pnServiceDeskOperations;
    }

}
