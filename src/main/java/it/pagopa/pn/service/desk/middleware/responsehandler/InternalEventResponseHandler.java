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
        try {
            log.logStartingProcess("VALIDATION OPERATION");
            this.validationOperationAction.execute(response.getOperationId());
            log.logEndingProcess("VALIDATION OPERATION");
        } catch (Exception ex) {
            log.logEndingProcess("ENDING WITH EXCEPTION");
        }
    }

}
