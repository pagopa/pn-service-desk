package it.pagopa.pn.service.desk.middleware.responsehandler;



import it.pagopa.pn.service.desk.action.ValidationOperationAction;
import it.pagopa.pn.service.desk.middleware.queue.model.InternalEventBody;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;

@Component
@CustomLog
@AllArgsConstructor
public class InternalEventResponseHandler {
    private ValidationOperationAction validationOperationAction;


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
