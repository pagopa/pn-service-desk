package it.pagopa.pn.service.desk.config.msclient;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.ApiClient;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.api.DigitalCourtesyMessagesApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExternalChannelApiConfigurator extends CommonBaseClient {

    @Bean
    public DigitalCourtesyMessagesApi getDigitalCoutesyMessageApiExternalChannel (PnServiceDeskConfigs pnServiceDeskConfigs){

        ApiClient apiClient =
                new ApiClient(super.initWebClient(ApiClient.buildWebClientBuilder()));

apiClient.setBasePath(pnServiceDeskConfigs.getExternalChannelBasePath());
apiClient.addDefaultHeader("x-pagopa-externalchannel-cx-id", pnServiceDeskConfigs.getExternalChannelCxId());

        return new DigitalCourtesyMessagesApi(apiClient);
    }

}
