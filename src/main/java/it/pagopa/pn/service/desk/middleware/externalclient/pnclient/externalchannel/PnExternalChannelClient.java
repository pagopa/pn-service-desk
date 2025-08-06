package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.externalchannel;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.dto.DigitalCourtesyMailRequestDto;
import reactor.core.publisher.Mono;

public interface PnExternalChannelClient {

    Mono<Void> sendCourtesyMail(String requestId, String xPagopaExtchCxId, DigitalCourtesyMailRequestDto digitalCourtesyMailRequestDto);

}
