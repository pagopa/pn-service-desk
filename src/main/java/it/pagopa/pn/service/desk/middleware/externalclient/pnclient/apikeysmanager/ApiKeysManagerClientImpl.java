package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.apikeysmanager;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pnapikeymanager.v1.api.ApiKeysBoApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnapikeymanager.v1.dto.ResponseApiKeysDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@AllArgsConstructor
public class ApiKeysManagerClientImpl implements ApiKeysManagerClient{

    private ApiKeysBoApi apiKeysBoApi;

    @Override
    public Mono<ResponseApiKeysDto> getBoApiKeys(String paId) {
        return apiKeysBoApi.getBoApiKeys(paId);
    }

}