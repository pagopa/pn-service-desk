package it.pagopa.pn.service.desk.service;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.ProfileRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.ProfileResponse;
import reactor.core.publisher.Mono;

public interface ProfileService {

    Mono<ProfileResponse> getProfileFromTaxId(String xPagopaPnUid, ProfileRequest profileRequest);
}
