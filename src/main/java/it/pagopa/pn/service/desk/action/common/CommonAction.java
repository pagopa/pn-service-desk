package it.pagopa.pn.service.desk.action.common;

import it.pagopa.pn.api.dto.events.GenericEventHeader;
import it.pagopa.pn.service.desk.middleware.queue.model.InternalEvent;
import it.pagopa.pn.service.desk.middleware.queue.model.InternalEventBody;
import it.pagopa.pn.service.desk.model.EventTypeEnum;
import it.pagopa.pn.service.desk.utility.Const;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class CommonAction {

    protected InternalEvent getInternalEvent(List<String> iuns, String operationId, String recipientInternalId){
        return getInternalEvent(iuns, operationId, recipientInternalId, 0);
    }

    protected InternalEvent getInternalEvent(List<String> iuns, String operationId, String recipientInternalId, int attempt){
        GenericEventHeader prepareHeader= GenericEventHeader.builder()
                .publisher(Const.PUBLISHER_PREPARE)
                .eventId(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .eventType(EventTypeEnum.NOTIFY_DELIVERY_PUSH.name())
                .build();

        InternalEventBody internalEventBody = new InternalEventBody();
        internalEventBody.setAttempt(attempt);
        internalEventBody.setOperationId(operationId);
        internalEventBody.setRecipientInternalId(recipientInternalId);
        internalEventBody.setIuns(iuns);
        return new InternalEvent(prepareHeader, internalEventBody);
    }
}
