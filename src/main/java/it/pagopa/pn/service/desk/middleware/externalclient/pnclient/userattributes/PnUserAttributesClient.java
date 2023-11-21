package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.userattributes;


import it.pagopa.pn.service.desk.generated.openapi.msclient.pnuserattributes.v1.dto.CourtesyDigitalAddressDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnuserattributes.v1.dto.LegalDigitalAddressDto;
import reactor.core.publisher.Flux;

public interface PnUserAttributesClient {

    Flux<LegalDigitalAddressDto> getLegalAddressBySender(String recipientId, String senderId);
    Flux<CourtesyDigitalAddressDto> getCourtesyAddressBySender(String recipientId, String senderId);
}
