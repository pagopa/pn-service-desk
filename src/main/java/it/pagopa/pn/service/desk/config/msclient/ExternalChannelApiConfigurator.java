package it.pagopa.pn.service.desk.config.msclient;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.ApiClient;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.api.DigitalCourtesyMessagesApi;
import it.pagopa.pn.service.desk.mapper.ExternalChannelMapper;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.templatesengine.PnTemplatesEngineClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class ExternalChannelApiConfigurator extends CommonBaseClient {

    private final PnTemplatesEngineClient pnTemplatesEngineClient;

    public ExternalChannelApiConfigurator(PnTemplatesEngineClient pnTemplatesEngineClient) {
        this.pnTemplatesEngineClient = pnTemplatesEngineClient;
    }

    @Bean
    public DigitalCourtesyMessagesApi getDigitalCoutesyMessageApiExternalChannel (PnServiceDeskConfigs pnServiceDeskConfigs){

        ApiClient apiClient =
                new ApiClient(super.initWebClient(ApiClient.buildWebClientBuilder()));

        apiClient.setBasePath(pnServiceDeskConfigs.getExternalChannelBasePath());

        return new DigitalCourtesyMessagesApi(apiClient);
    }

    @PostConstruct
    public void init() {
        ExternalChannelMapper.setPnTemplatesEngineClient(pnTemplatesEngineClient);
    }

}
