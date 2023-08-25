package it.pagopa.pn.service.desk.config.msclient;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.ApiClient;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.api.PaperNotificationFailedApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.api.LegalFactsPrivateApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeliveryPushApiConfigurator extends CommonBaseClient {

    @Bean
    public PaperNotificationFailedApi getPaperNotificationFailedApiDeliveryPush(PnServiceDeskConfigs pnServiceDeskConfigs){
        ApiClient apiClient =
                new ApiClient(super.initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(pnServiceDeskConfigs.getDeliveryPushBasePath());
        return new PaperNotificationFailedApi(apiClient);
    }

    @Bean
    public LegalFactsPrivateApi getLegalFactsPrivateApiDeliveryPush(PnServiceDeskConfigs pnServiceDeskConfigs){
        ApiClient apiClient =
                new ApiClient(super.initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(pnServiceDeskConfigs.getDeliveryPushBasePath());
        return new LegalFactsPrivateApi(apiClient);
    }
}
