package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.addressmanager;

import it.pagopa.pn.paperchannel.generated.openapi.msclient.pnaddressmanager.v1.dto.DeduplicatesResponseDto;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import reactor.core.publisher.Mono;

public interface PnAddressManagerClient {


    Mono<DeduplicatesResponseDto> deduplicates(PnServiceDeskAddress address);

}
