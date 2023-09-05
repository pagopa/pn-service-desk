package it.pagopa.pn.service.desk.config.msclient;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnaddressmanager.v1.ApiClient;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnaddressmanager.v1.api.DeduplicatesAddressServiceApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AddressManagerApiConfigurator extends CommonBaseClient {

    @Bean
    public DeduplicatesAddressServiceApi getDeduplicatesAddressServiceApiAddress(PnServiceDeskConfigs pnServiceDeskConfigs){
        ApiClient apiClient =
                new ApiClient(super.initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(pnServiceDeskConfigs.getAddressManagerBasePath());
        return new DeduplicatesAddressServiceApi(apiClient);
    }
}
