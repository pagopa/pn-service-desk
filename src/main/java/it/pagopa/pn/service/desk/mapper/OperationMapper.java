package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.CreateOperationRequest;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

import java.time.Instant;

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

}
