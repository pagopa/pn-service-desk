package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.externalregistries;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.v1.api.InfoPaApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.v1.dto.PaSummaryDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;


@Slf4j
@Component
@AllArgsConstructor
public class ExternalRegistriesClientImpl implements ExternalRegistriesClient {

    private InfoPaApi infoPaApi;


    @Override
    public Flux<PaSummaryDto> listOnboardedPa() {
        return infoPaApi.listOnboardedPa(null, null);
    }
}
