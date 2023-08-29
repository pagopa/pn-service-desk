package it.pagopa.pn.service.desk.middleware.responsehandler;



import it.pagopa.pn.service.desk.action.common.BaseAction;
import it.pagopa.pn.service.desk.middleware.queue.model.InternalEventBody;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@CustomLog
public class InternalEventResponseHandler {

    @Autowired
    @Qualifier("ValidationAction")
    private BaseAction<String> validationOperationAction;


    public void handleInternalEventResponse(InternalEventBody response) {
        this.validationOperationAction.execute(response.getOperationId());
    }

}
