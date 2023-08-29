package it.pagopa.pn.service.desk.action;


import it.pagopa.pn.service.desk.action.common.BaseAction;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.SendEventDto;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Qualifier("ResultAction")
@Component
@CustomLog
public class ResultPaperChannelAction implements BaseAction<SendEventDto> {

    @Override
    public void execute(SendEventDto sendEventDto) {

    }
}
