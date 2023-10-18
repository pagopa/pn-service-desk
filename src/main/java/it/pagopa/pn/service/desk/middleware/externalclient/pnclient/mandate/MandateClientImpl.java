package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.mandate;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pnmandate.v1.api.MandatePrivateServiceApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnmandate.v1.dto.InternalMandateDtoDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;


@Slf4j
@Component
@AllArgsConstructor
public class MandateClientImpl implements MandateClient{

    private MandatePrivateServiceApi mandatePrivateServiceApi;
    @Override
    public Flux<InternalMandateDtoDto> listMandatesByDelegate(String internaluserId) {
        return mandatePrivateServiceApi.listMandatesByDelegate(internaluserId, null, null, null);
    }

    @Override
    public Flux<InternalMandateDtoDto> listMandatesByDelegator(String internaluserId) {
        return mandatePrivateServiceApi.listMandatesByDelegator(internaluserId, null, null, null, null, null);
    }
}
