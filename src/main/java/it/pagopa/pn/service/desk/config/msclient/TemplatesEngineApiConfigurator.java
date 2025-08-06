package it.pagopa.pn.service.desk.config.msclient;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pntemplatesengine.v1.ApiClient;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pntemplatesengine.v1.api.TemplateApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemplatesEngineApiConfigurator extends CommonBaseClient {


    @Bean
    public TemplateApi getTemplateEngineApi(PnServiceDeskConfigs pnServiceDeskConfigs){
        ApiClient apiClient =
                new ApiClient(super.initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(pnServiceDeskConfigs.getTemplatesBasePath());

        return new TemplateApi(apiClient);
    }
}
