package it.pagopa.pn.service.desk.middleware.responsehandler;

import it.pagopa.pn.service.desk.action.PreparePaperChannelAction;
import it.pagopa.pn.service.desk.action.ResultPaperChannelAction;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.PrepareEventDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.SendEventDto;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;

@Component
@CustomLog
@AllArgsConstructor
public class PaperChannelResponseHandler {
    private PreparePaperChannelAction prepareEventAction;
    private ResultPaperChannelAction resultEventAction;

    public void handlePreparePaperChannelEventResponse(PrepareEventDto eventDto){
        prepareEventAction.execute(eventDto);
    }


    public void handleResultPaperChannelEventResponse(SendEventDto eventDto){
        resultEventAction.execute(eventDto);
    }
}
