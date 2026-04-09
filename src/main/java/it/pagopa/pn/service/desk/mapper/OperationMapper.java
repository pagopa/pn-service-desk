package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
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

    public static PnServiceDeskOperations getInitialOperation(CreateOperationRequest operationRequest, String recipientInternalId) {
        PnServiceDeskOperations pnServiceDeskOperations = new PnServiceDeskOperations();
        pnServiceDeskOperations.setOperationId(Utility.generateOperationId(operationRequest.getTicketId(), operationRequest.getTicketOperationId()));
        pnServiceDeskOperations.setTicketId(operationRequest.getTicketId());
        pnServiceDeskOperations.setStatus(OperationStatusEnum.CREATING.toString());
        pnServiceDeskOperations.setOperationStartDate(Instant.now());
        pnServiceDeskOperations.setOperationLastUpdateDate(Instant.now());
        pnServiceDeskOperations.setRecipientInternalId(recipientInternalId);

        return pnServiceDeskOperations;    }

    public static PnServiceDeskOperations getInitialActOperation(CreateActOperationRequest operationRequest, String recipientInternalId) {
        PnServiceDeskOperations pnServiceDeskOperations = new PnServiceDeskOperations();
        pnServiceDeskOperations.setOperationId(Utility.generateOperationId(operationRequest.getTicketId(), operationRequest.getTicketOperationId()));
        pnServiceDeskOperations.setTicketId(operationRequest.getTicketId());
        pnServiceDeskOperations.setStatus(OperationStatusEnum.CREATING.toString());
        pnServiceDeskOperations.setOperationStartDate(Instant.now());
        pnServiceDeskOperations.setOperationLastUpdateDate(Instant.now());
        pnServiceDeskOperations.setRecipientInternalId(recipientInternalId);
        pnServiceDeskOperations.setTicketDate(operationRequest.getTicketDate());
        pnServiceDeskOperations.setVrDate(operationRequest.getVrDate());
        pnServiceDeskOperations.setIun(operationRequest.getIun());
        return pnServiceDeskOperations;
    }

    public static OperationResponse operationResponseMapper(PnServiceDeskOperations pnServiceDeskOperations, String taxId){
        OperationResponse operationResponse = new OperationResponse();
        List<SDNotificationSummary> iunsList = new ArrayList<>();
        List<SDNotificationSummary> uncompletedIunsList = new ArrayList<>();

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

        operationResponse.setOperationId(Utility.cleanUpOperationId(pnServiceDeskOperations.getOperationId()));
        operationResponse.setIuns(iunsList);
        operationResponse.setUncompletedIuns(uncompletedIunsList);
        operationResponse.setOperationCreateTimestamp(OffsetDateTime.ofInstant(pnServiceDeskOperations.getOperationStartDate(), ZoneOffset.UTC));
        if (pnServiceDeskOperations.getOperationLastUpdateDate() != null) {
            operationResponse.setOperationUpdateTimestamp( OffsetDateTime.ofInstant(pnServiceDeskOperations.getOperationLastUpdateDate(), ZoneOffset.UTC));
        }
        operationResponse.setNotificationStatus(status);
        operationResponse.setTaxId(taxId);

        return operationResponse;
    }

    public static PnServiceDeskOperations getInitialParentOperation(CreateActOperationRequestV2 request,
                                                                    String recipientInternalId,
                                                                    String operationId,
                                                                    List<String> subOperationIds) {
        PnServiceDeskOperations pnServiceDeskOperations = new PnServiceDeskOperations();
        pnServiceDeskOperations.setOperationId(operationId);
        pnServiceDeskOperations.setTicketId(request.getTicketId());
        pnServiceDeskOperations.setStatus(OperationStatusEnum.CREATING.toString());
        pnServiceDeskOperations.setOperationStartDate(Instant.now());
        pnServiceDeskOperations.setOperationLastUpdateDate(Instant.now());
        pnServiceDeskOperations.setRecipientInternalId(recipientInternalId);
        pnServiceDeskOperations.setTicketDate(request.getTicketDate());
        pnServiceDeskOperations.setVrDate(request.getVrDate());
        pnServiceDeskOperations.setIsSubOperation(false);
        pnServiceDeskOperations.setSubOperationsIds(subOperationIds);
        return pnServiceDeskOperations;
    }

    public static PnServiceDeskOperations getInitialSubOperation(String parentOperationId,
                                                                  String iun,
                                                                  String recipientInternalId,
                                                                  CreateActOperationRequestV2 request) {
        return createSubOperation(parentOperationId, iun, recipientInternalId, OperationStatusEnum.CREATING, request);
    }

    public static PnServiceDeskOperations getFailedSubOperation(String parentOperationId,
                                                                String iun,
                                                                String recipientInternalId,
                                                                String errorReason,
                                                                CreateActOperationRequestV2 request) {
        PnServiceDeskOperations subOperation = createSubOperation(parentOperationId, iun, recipientInternalId, OperationStatusEnum.KO, request);
        subOperation.setErrorReason(errorReason);
        return subOperation;
    }

    private static PnServiceDeskOperations createSubOperation(String parentOperationId,
                                                             String iun,
                                                             String recipientInternalId,
                                                             OperationStatusEnum status,
                                                             CreateActOperationRequestV2 request) {
        PnServiceDeskOperations subOperation = new PnServiceDeskOperations();
        subOperation.setOperationId("SUB#" + parentOperationId + "#" + iun);
        subOperation.setTicketId(request.getTicketId());
        subOperation.setStatus(status.toString());
        subOperation.setOperationStartDate(Instant.now());
        subOperation.setOperationLastUpdateDate(Instant.now());
        subOperation.setRecipientInternalId(recipientInternalId);
        subOperation.setTicketDate(request.getTicketDate());
        subOperation.setVrDate(request.getVrDate());
        subOperation.setIun(iun);
        subOperation.setIsSubOperation(true);
        return subOperation;
    }

    public static PnServiceDeskOperations copyOperation (PnServiceDeskOperations operations){
        PnServiceDeskOperations pnServiceDeskOperations = new PnServiceDeskOperations();
        BeanUtils.copyProperties(operations, pnServiceDeskOperations);
        return pnServiceDeskOperations;
    }

}