package it.pagopa.pn.service.desk.middleware.responsehandler;

import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;


@Component
@CustomLog
@AllArgsConstructor
public class SafeStorageResponseHandler {

    public void handleSafeStorageResponse(FileDownloadResponse response) {
        //TODO
    }

}
