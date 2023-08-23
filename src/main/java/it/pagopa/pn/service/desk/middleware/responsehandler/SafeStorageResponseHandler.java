package it.pagopa.pn.service.desk.middleware.responsehandler;

import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.service.desk.middleware.queue.model.InternalEvent;
import it.pagopa.pn.service.desk.middleware.queue.producer.InternalQueueMomProducer;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@CustomLog
@AllArgsConstructor
public class SafeStorageResponseHandler {

    @Autowired
    private InternalQueueMomProducer internalQueueMomProducer;

    public void handleSafeStorageResponse(FileDownloadResponse response) {
        //TODO
        internalQueueMomProducer.push(getInternalEvent(""));
    }


    private InternalEvent getInternalEvent(String operationId){return null;}

}
