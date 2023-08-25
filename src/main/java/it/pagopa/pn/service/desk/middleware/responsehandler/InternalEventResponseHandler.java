package it.pagopa.pn.service.desk.middleware.responsehandler;



import it.pagopa.pn.service.desk.action.ValidationOperationAction;
import it.pagopa.pn.service.desk.middleware.queue.model.InternalEventBody;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@CustomLog
@AllArgsConstructor
public class InternalEventResponseHandler {

    @Autowired
    private ValidationOperationAction validationOperationAction;

    public void handleInternalEventResponse(InternalEventBody response) {
        this.validationOperationAction.validateOperation(response.getOperationId());
    }

}
