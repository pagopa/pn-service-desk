package it.pagopa.pn.service.desk.config.msclient;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.paperchannel.generated.openapi.msclient.pnaddressmanager.v1.ApiClient;
import it.pagopa.pn.paperchannel.generated.openapi.msclient.pnaddressmanager.v1.api.DeduplicatesAddressServiceApi;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import org.springframework.context.annotation.Bean;

public class AddressManagerApiConfigarator extends CommonBaseClient {

    @Bean
    public DeduplicatesAddressServiceApi getRecipientsApi(PnServiceDeskConfigs pnServiceDeskConfigs){
        ApiClient apiClient =
                new ApiClient(super.initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(pnServiceDeskConfigs.getAddressManagerBasePath());
        return new DeduplicatesAddressServiceApi(apiClient);
    }
}
