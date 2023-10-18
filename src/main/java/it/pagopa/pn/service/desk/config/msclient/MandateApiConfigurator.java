package it.pagopa.pn.service.desk.config.msclient;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnmandate.v1.ApiClient;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnmandate.v1.api.MandatePrivateServiceApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MandateApiConfigurator extends CommonBaseClient {

    @Bean
    public MandatePrivateServiceApi getPrivateServiceApiMandate (PnServiceDeskConfigs pnServiceDeskConfigs){
        ApiClient apiClient =
                new ApiClient(super.initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(pnServiceDeskConfigs.getMandateBasePath());
        return new MandatePrivateServiceApi(apiClient);
    }
}
