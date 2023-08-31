package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.paperchannel;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.PaperChannelUpdateDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.PrepareRequestDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.SendRequestDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.SendResponseDto;
import reactor.core.publisher.Mono;

public interface PnPaperChannelClient {

    Mono<PaperChannelUpdateDto> sendPaperPrepareRequest(String requestId, PrepareRequestDto prepareRequestDto);

    Mono<SendResponseDto> sendPaperSendRequest(String requestId, SendRequestDto sendRequestDto);
}
