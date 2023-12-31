package it.pagopa.pn.service.desk.middleware.responsehandler;

import it.pagopa.pn.api.dto.events.GenericEventHeader;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationsFileKeyDAO;
import it.pagopa.pn.service.desk.middleware.queue.model.InternalEvent;
import it.pagopa.pn.service.desk.middleware.queue.model.InternalEventBody;
import it.pagopa.pn.service.desk.middleware.queue.producer.InternalQueueMomProducer;
import it.pagopa.pn.service.desk.model.EventTypeEnum;
import it.pagopa.pn.service.desk.utility.Const;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;


@Component
@CustomLog
@AllArgsConstructor
public class SafeStorageResponseHandler {

    private InternalQueueMomProducer internalQueueMomProducer;
    private OperationsFileKeyDAO operationsFileKeyDAO;

    public void handleSafeStorageResponse(FileDownloadResponse response) {
        operationsFileKeyDAO.getOperationFileKey(response.getKey())
                .doOnNext(operationFileKey ->
                        internalQueueMomProducer.push(getInternalEvent(operationFileKey.getOperationId()))
                ).block();
    }


    private InternalEvent getInternalEvent(String operationId){

        GenericEventHeader prepareHeader= GenericEventHeader.builder()
                .publisher(Const.PUBLISHER_PREPARE)
                .eventId(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .eventType(EventTypeEnum.VALIDATION_OPERATIONS_EVENTS.name())
                .build();

        InternalEventBody internalEventBody = new InternalEventBody();
        internalEventBody.setOperationId(operationId);

        return new InternalEvent(prepareHeader, internalEventBody);
    }

}
