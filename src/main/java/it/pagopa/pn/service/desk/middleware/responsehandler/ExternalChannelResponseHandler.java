package it.pagopa.pn.service.desk.middleware.responsehandler;

import it.pagopa.pn.service.desk.action.ResultExternalChannelAction;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.dto.CourtesyMessageProgressEventDto;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;

@Component
@CustomLog
@AllArgsConstructor
public class ExternalChannelResponseHandler {
    private ResultExternalChannelAction  resultEventAction;

  public void handleResultExternalChannelEventResponse(CourtesyMessageProgressEventDto eventDto) {
        resultEventAction.execute(eventDto);
    }



}
