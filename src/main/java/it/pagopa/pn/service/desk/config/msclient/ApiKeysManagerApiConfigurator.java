package it.pagopa.pn.service.desk.config.msclient;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnapikeymanager.v1.ApiClient;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnapikeymanager.v1.api.ApiKeysBoApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiKeysManagerApiConfigurator extends CommonBaseClient{

    @Bean
    public ApiKeysBoApi getApiKeysManagerApi(PnServiceDeskConfigs pnServiceDeskConfigs){
        ApiClient apiClient =
                new ApiClient(super.initWebClient(it.pagopa.pn.service.desk.generated.openapi.msclient.pnmandate.v1.ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(pnServiceDeskConfigs.getApiKeyManagerBasePath());
        return new ApiKeysBoApi(apiClient);
    }

}