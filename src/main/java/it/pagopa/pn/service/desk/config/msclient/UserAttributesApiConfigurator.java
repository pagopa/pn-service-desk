package it.pagopa.pn.service.desk.config.msclient;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnuserattributes.v1.ApiClient;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnuserattributes.v1.api.CourtesyApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnuserattributes.v1.api.LegalApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserAttributesApiConfigurator extends CommonBaseClient {

    @Bean
    public LegalApi getLegalApiUserAttributes (PnServiceDeskConfigs pnServiceDeskConfigs){
        ApiClient apiClient =
                new ApiClient(super.initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(pnServiceDeskConfigs.getUserAttributesBasePath());
        return new LegalApi(apiClient);
    }

    @Bean
    public CourtesyApi getCourtesyApiUserAttributes (PnServiceDeskConfigs pnServiceDeskConfigs){
        ApiClient apiClient =
                new ApiClient(super.initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(pnServiceDeskConfigs.getUserAttributesBasePath());
        return new CourtesyApi(apiClient);
    }
}
