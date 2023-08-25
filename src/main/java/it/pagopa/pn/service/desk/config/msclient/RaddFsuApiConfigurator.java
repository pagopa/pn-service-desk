package it.pagopa.pn.service.desk.config.msclient;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnraddfsu.v1.ApiClient;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnraddfsu.v1.api.AorDocumentInquiryApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RaddFsuApiConfigurator extends CommonBaseClient {

    @Bean
    public AorDocumentInquiryApi getAorDocumentInquiryApiRaddFsu(PnServiceDeskConfigs pnServiceDeskConfigs){
        ApiClient apiClient =
                new ApiClient(super.initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(pnServiceDeskConfigs.getRaddFsuBasePath());
        return new AorDocumentInquiryApi(apiClient);
    }
}
