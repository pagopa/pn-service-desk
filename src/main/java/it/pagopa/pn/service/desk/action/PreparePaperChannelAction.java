package it.pagopa.pn.service.desk.action;

import it.pagopa.pn.service.desk.action.common.BaseAction;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.PrepareEventDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("PrepareAction")
public class PreparePaperChannelAction implements BaseAction<PrepareEventDto> {



    @Override
    public void execute(PrepareEventDto eventDto) {

    }
}
