package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.userattributes;


import it.pagopa.pn.service.desk.generated.openapi.msclient.pnuserattributes.v1.api.CourtesyApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnuserattributes.v1.api.LegalApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnuserattributes.v1.dto.CourtesyDigitalAddressDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnuserattributes.v1.dto.LegalDigitalAddressDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;


@Slf4j
@Component
@AllArgsConstructor
public class PnUserAttributesClientImpl implements PnUserAttributesClient {

    private LegalApi legalApi;
    private CourtesyApi courtesyApi;

    @Override
    public Flux<LegalDigitalAddressDto> getLegalAddressBySender(String recipientId, String senderId) {
        return legalApi.getLegalAddressBySender(recipientId, senderId);
    }

    @Override
    public Flux<CourtesyDigitalAddressDto> getCourtesyAddressBySender(String recipientId, String senderId) {
        return courtesyApi.getCourtesyAddressBySender(recipientId, senderId);
    }
}
