package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.externalregistries;


import it.pagopa.pn.platform.msclient.generated.pnexternalregistries.v1.dto.PaSummaryDto;
import reactor.core.publisher.Flux;


public interface ExternalRegistriesClient {
    Flux<PaSummaryDto> listOnboardedPa();

}
