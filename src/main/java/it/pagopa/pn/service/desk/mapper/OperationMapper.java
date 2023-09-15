package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationStatus;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.OperationResponse;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.SDNotificationSummary;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.CreateOperationRequest;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskEvents;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import it.pagopa.pn.service.desk.utility.Utility;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class OperationMapper {

    private OperationMapper(){}

    public static PnServiceDeskOperations getInitialOperation (CreateOperationRequest operationRequest, String recipientInternalId){

        PnServiceDeskOperations pnServiceDeskOperations = new PnServiceDeskOperations();
        pnServiceDeskOperations.setOperationId(Utility.generateOperationId(operationRequest.getTicketId(), operationRequest.getTicketOperationId()));
        pnServiceDeskOperations.setTicketId(operationRequest.getTicketId());
        pnServiceDeskOperations.setStatus(OperationStatusEnum.CREATING.toString());
        pnServiceDeskOperations.setOperationStartDate(Instant.now());
        pnServiceDeskOperations.setOperationLastUpdateDate(Instant.now());
        pnServiceDeskOperations.setRecipientInternalId(recipientInternalId);

        return pnServiceDeskOperations;
    }

    public static OperationResponse operationResponseMapper(PnServiceDeskOperations pnServiceDeskOperations){
        OperationResponse operationResponse = new OperationResponse();
        operationResponse.setOperationId(pnServiceDeskOperations.getOperationId());

        List<SDNotificationSummary> iunsList = new ArrayList<>();
        List<SDNotificationSummary> uncompletedIunsList = new ArrayList<>();
        operationResponse.setIuns(iunsList);
        operationResponse.setUncompletedIuns(uncompletedIunsList);

        List<PnServiceDeskAttachments> attachments = pnServiceDeskOperations.getAttachments();
        if (attachments != null) {
            attachments.forEach(att -> {
                SDNotificationSummary summary = new SDNotificationSummary();
                summary.setIun(att.getIun());
                if (Boolean.TRUE.equals(att.getIsAvailable())) {
                    operationResponse.getIuns().add(summary);
                } else {
                    operationResponse.getUncompletedIuns().add(summary);
                }
            });
        }
        operationResponse.setOperationCreateTimestamp(OffsetDateTime.ofInstant(pnServiceDeskOperations.getOperationStartDate(), ZoneOffset.UTC));
        operationResponse.setOperationUpdateTimestamp( OffsetDateTime.ofInstant(pnServiceDeskOperations.getOperationLastUpdateDate(), ZoneOffset.UTC));
        NotificationStatus status = new NotificationStatus();
        status.setStatus(NotificationStatus.StatusEnum.fromValue(pnServiceDeskOperations.getStatus()));
        status.setStatusDescription(pnServiceDeskOperations.getErrorReason());
        if (pnServiceDeskOperations.getEvents() != null && !pnServiceDeskOperations.getEvents().isEmpty()) {
            PnServiceDeskEvents e = pnServiceDeskOperations.getEvents().stream()
                    .max(Comparator.comparing(PnServiceDeskEvents::getTimestamp))
                    .orElse(new PnServiceDeskEvents());
            status.setStatusCode(e.getStatusCode());
            if (e.getTimestamp() != null) status.setLastEventTimestamp(Utility.getOffsetDateTimeFromDate(e.getTimestamp()));
        }

        operationResponse.setNotificationStatus(status);
        operationResponse.setTaxId(pnServiceDeskOperations.getRecipientInternalId());

        return operationResponse;
    }

}
