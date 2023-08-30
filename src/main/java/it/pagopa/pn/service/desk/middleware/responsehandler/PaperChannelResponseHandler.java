package it.pagopa.pn.service.desk.middleware.responsehandler;

import it.pagopa.pn.service.desk.action.common.BaseAction;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.PrepareEventDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.SendEventDto;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@CustomLog
public class PaperChannelResponseHandler {

    @Autowired
    @Qualifier("PrepareAction")
    private BaseAction<PrepareEventDto> prepareEventAction;
    @Autowired
    @Qualifier("ResultAction")
    private BaseAction<SendEventDto> resultEventAction;

    public void handlePreparePaperChannelEventResponse(PrepareEventDto eventDto){
        prepareEventAction.execute(eventDto);
    }


    public void handleResultPaperChannelEventResponse(SendEventDto eventDto){
        resultEventAction.execute(eventDto);
    }
}
