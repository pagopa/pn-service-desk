package it.pagopa.pn.service.desk.action.impl;

import it.pagopa.pn.service.desk.action.ResultPaperChannelAction;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.SendEventDto;
import lombok.CustomLog;
import org.springframework.stereotype.Component;

@Component
@CustomLog
public class ResultPaperChannelActionImpl implements ResultPaperChannelAction {



    @Override
    public void execute(SendEventDto sendEventDto) {

    }

}
