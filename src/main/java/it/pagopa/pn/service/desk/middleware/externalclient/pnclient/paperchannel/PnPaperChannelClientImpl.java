package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.paperchannel;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.api.PaperMessagesApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.PaperChannelUpdateDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.PrepareRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@Component
public class PnPaperChannelClientImpl implements PnPaperChannelClient{

    @Autowired
    private PaperMessagesApi paperMessagesApi;
    @Override
    public Mono<PaperChannelUpdateDto> sendPaperPrepareRequest(String requestId, PrepareRequestDto prepareRequestDto) {
        return paperMessagesApi.sendPaperPrepareRequest(requestId, prepareRequestDto);
    }
}
