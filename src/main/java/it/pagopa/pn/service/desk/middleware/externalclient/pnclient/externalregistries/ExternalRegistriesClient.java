package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.externalregistries;


import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.v1.dto.PaSummaryDto;
import reactor.core.publisher.Flux;


public interface ExternalRegistriesClient {
    Flux<PaSummaryDto> listOnboardedPa();

}
