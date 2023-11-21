package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.mandate;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pnmandate.v1.dto.InternalMandateDtoDto;
import reactor.core.publisher.Flux;


public interface MandateClient {
    Flux<InternalMandateDtoDto> listMandatesByDelegate(String internaluserId);
    Flux<InternalMandateDtoDto> listMandatesByDelegator(String internaluserId);
}
