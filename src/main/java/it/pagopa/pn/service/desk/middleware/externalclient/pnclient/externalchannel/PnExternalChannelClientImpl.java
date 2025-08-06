package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.externalchannel;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.api.DigitalCourtesyMessagesApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.dto.DigitalCourtesyMailRequestDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class PnExternalChannelClientImpl implements PnExternalChannelClient{

    private DigitalCourtesyMessagesApi digitalCourtesyMessagesApi;


    @Override
    public Mono<Void> sendCourtesyMail(String requestId, String xPagopaExtchCxId, DigitalCourtesyMailRequestDto digitalCourtesyMailRequestDto) {
        return digitalCourtesyMessagesApi.sendDigitalCourtesyMessage(requestId, xPagopaExtchCxId, digitalCourtesyMailRequestDto );
    }


}
