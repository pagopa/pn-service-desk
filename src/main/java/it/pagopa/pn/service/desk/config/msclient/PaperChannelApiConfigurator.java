package it.pagopa.pn.service.desk.config.msclient;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.ApiClient;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.api.PaperMessagesApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaperChannelApiConfigurator extends CommonBaseClient {

    @Bean
    public PaperMessagesApi getPaperMessagesApiPaperChannel(PnServiceDeskConfigs pnServiceDeskConfigs){
        ApiClient apiClient =
                new ApiClient(super.initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(pnServiceDeskConfigs.getPaperChannelBasePath());
        return new PaperMessagesApi(apiClient);
    }
}
