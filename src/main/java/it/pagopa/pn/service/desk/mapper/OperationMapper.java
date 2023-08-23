package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationStatus;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.OperationResponse;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.SDNotificationSummary;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.CreateOperationRequest;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import it.pagopa.pn.service.desk.service.OperationsService;
import jdk.dynalink.Operation;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class OperationMapper {

    public static PnServiceDeskOperations getInitialOperation (CreateOperationRequest operationRequest, String recipientInternalId){

        PnServiceDeskOperations pnServiceDeskOperations = new PnServiceDeskOperations();
        pnServiceDeskOperations.setOperationId(getOperationId(operationRequest));
        pnServiceDeskOperations.setTicketId(operationRequest.getTicketId());
        pnServiceDeskOperations.setStatus(OperationStatusEnum.CREATING.toString());
        pnServiceDeskOperations.setOperationStartDate(Instant.now());
        pnServiceDeskOperations.setOperationLastUpdateDate(Instant.now());
        pnServiceDeskOperations.setRecipientInternalId(recipientInternalId);

        return pnServiceDeskOperations;
    }


    private static String getOperationId(CreateOperationRequest operationRequest){
        String suffix = "000";
        if (StringUtils.isBlank(operationRequest.getTicketOperationId()))
            suffix = operationRequest.getTicketOperationId();
        return operationRequest.getTicketId().concat(suffix);
    }

    public static OperationResponse operationResponseMapper(PnServiceDeskOperations pnServiceDeskOperations){

        OperationResponse operationResponse = new OperationResponse();

        operationResponse.setOperationId(pnServiceDeskOperations.getOperationId());
        List<PnServiceDeskAttachments> attachments = pnServiceDeskOperations.getAttachments();
        attachments.forEach(att -> {
            SDNotificationSummary summary = new SDNotificationSummary();
            summary.setIun(att.getIun());
            if (att.getIsAvailable()) {
                operationResponse.getIuns().add(summary);
            } else {
                operationResponse.getUncompletedIuns().add(summary);
            }
        });
        operationResponse.setOperationCreateTimestamp(OffsetDateTime.ofInstant(pnServiceDeskOperations.getOperationStartDate(), ZoneOffset.UTC));
        operationResponse.setOperationUpdateTimestamp( OffsetDateTime.ofInstant(pnServiceDeskOperations.getOperationLastUpdateDate(), ZoneOffset.UTC));
        //operationResponse.setNotificationStatus();
        operationResponse.setTaxId(pnServiceDeskOperations.getRecipientInternalId());

        return operationResponse;
    }

}
