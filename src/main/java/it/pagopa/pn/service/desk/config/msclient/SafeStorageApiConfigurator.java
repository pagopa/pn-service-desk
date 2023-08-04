package it.pagopa.pn.service.desk.config.msclient;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.ApiClient;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.api.FileDownloadApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.api.FileMetadataUpdateApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.api.FileUploadApi;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SafeStorageApiConfigurator extends CommonBaseClient {

    @Bean
    public FileUploadApi fileUploadApi(PnServiceDeskConfigs cfg){
        return new FileUploadApi( getNewApiClient(cfg) );
    }

    @Bean
    public FileDownloadApi fileDownloadApi(PnServiceDeskConfigs cfg){
        return new FileDownloadApi( getNewApiClient(cfg) );
    }

    @Bean
    public FileMetadataUpdateApi fileMetadataUpdateApi(PnServiceDeskConfigs cfg){
        return new FileMetadataUpdateApi( getNewApiClient(cfg) );
    }
    
    @NotNull
    private ApiClient getNewApiClient(PnServiceDeskConfigs cfg) {
        ApiClient newApiClient = new ApiClient( initWebClient(ApiClient.buildWebClientBuilder()) );
        newApiClient.setBasePath( cfg.getSafeStorageBaseUrl() );
        return newApiClient;
    }
}
