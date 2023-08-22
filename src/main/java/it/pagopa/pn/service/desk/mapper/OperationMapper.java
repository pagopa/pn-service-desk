package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import reactor.core.publisher.Mono;

import java.time.Instant;

public class OperationMapper {

    public static PnServiceDeskOperations getInitialOperation (String operationId, String recipientInternalId, String ticketId){

        PnServiceDeskOperations pnServiceDeskOperations = new PnServiceDeskOperations();
        pnServiceDeskOperations.setOperationId(operationId);
        pnServiceDeskOperations.setTicketId(ticketId);
        pnServiceDeskOperations.setStatus(OperationStatusEnum.CREATING.toString());
        pnServiceDeskOperations.setOperationStartDate(Instant.now());
        pnServiceDeskOperations.setOperationLastUpdateDate(Instant.now());
        pnServiceDeskOperations.setRecipientInternalId(recipientInternalId);

        return pnServiceDeskOperations;


    }
}
