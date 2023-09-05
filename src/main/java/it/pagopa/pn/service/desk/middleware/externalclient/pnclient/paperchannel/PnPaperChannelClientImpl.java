package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.paperchannel;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.api.PaperMessagesApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.PaperChannelUpdateDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.PrepareRequestDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.SendRequestDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.SendResponseDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@Component
@AllArgsConstructor
public class PnPaperChannelClientImpl implements PnPaperChannelClient{
    private PaperMessagesApi paperMessagesApi;

    @Override
    public Mono<PaperChannelUpdateDto> sendPaperPrepareRequest(String requestId, PrepareRequestDto prepareRequestDto) {
        return paperMessagesApi.sendPaperPrepareRequest(requestId, prepareRequestDto);
    }

    @Override
    public Mono<SendResponseDto> sendPaperSendRequest(String requestId, SendRequestDto sendRequestDto) {
        return paperMessagesApi.sendPaperSendRequest(requestId, sendRequestDto);
    }
}
