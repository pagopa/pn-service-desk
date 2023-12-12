package it.pagopa.pn.service.desk.rest;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.api.ProfileApi;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.ProfileRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.ProfileResponse;
import it.pagopa.pn.service.desk.service.ProfileService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
public class ProfileController implements ProfileApi {

    private final ProfileService profileService;
    @Override
    public Mono<ResponseEntity<ProfileResponse>> getProfileFromTaxId(String xPagopaPnUid, Mono<ProfileRequest> profileRequest, ServerWebExchange exchange) {
        return profileRequest.flatMap(request -> profileService.getProfileFromTaxId(xPagopaPnUid, request)
                .map(profileResponse ->  ResponseEntity.status(HttpStatus.OK).body(profileResponse)));
    }
}
