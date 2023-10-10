package it.pagopa.pn.service.desk.config.msclient;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.platform.msclient.generated.pnexternalregistries.v1.ApiClient;
import it.pagopa.pn.platform.msclient.generated.pnexternalregistries.v1.api.InfoPaApi;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExternalRegistriesApiConfigurator extends CommonBaseClient {

    @Bean
    public InfoPaApi getInfoPaApiExternalRegistries(PnServiceDeskConfigs pnServiceDeskConfigs){
        ApiClient apiClient =
                new ApiClient(super.initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(pnServiceDeskConfigs.getExternalRegistriesBasePath());
        return new InfoPaApi(apiClient);
    }
}
