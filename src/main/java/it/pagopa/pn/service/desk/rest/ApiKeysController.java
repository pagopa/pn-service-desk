package it.pagopa.pn.service.desk.rest;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.api.ApiKeysApi;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.ResponseApiKeys;
import it.pagopa.pn.service.desk.service.ApiKeysService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class ApiKeysController implements ApiKeysApi {

    @Autowired
    private ApiKeysService apiKeysService;

    @Override
    public Mono<ResponseEntity<ResponseApiKeys>> getApiKeys(String paId, ServerWebExchange exchange) {
        return apiKeysService.getApiKeys(paId)
                .map(responseApiKeys -> ResponseEntity.status(HttpStatus.OK).body(responseApiKeys));
    }
}