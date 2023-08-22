package it.pagopa.pn.service.desk.middleware.msclient.common;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndatavault.v1.api.RecipientsApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.ApiClient;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.api.FileDownloadApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.api.FileMetadataUpdateApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.api.FileUploadApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfig  extends CommonBaseClient {
    @Bean
    public FileDownloadApi getSafeStorageClient (PnServiceDeskConfigs pnServiceDeskConfigs){

        ApiClient newApiClient = new ApiClient(super.initWebClient(ApiClient.buildWebClientBuilder()));
        newApiClient.setBasePath(pnServiceDeskConfigs.getSafeStorageBaseUrl());

        return new FileDownloadApi(newApiClient);
    }

    @Bean
    public FileUploadApi getFileUploadAPI (PnServiceDeskConfigs pnServiceDeskConfigs){

        ApiClient newApiClient = new ApiClient(super.initWebClient(ApiClient.buildWebClientBuilder()));
        newApiClient.setBasePath(pnServiceDeskConfigs.getSafeStorageBaseUrl());

        return new FileUploadApi(newApiClient);
    }

    @Bean
    public FileMetadataUpdateApi getFileMetadataUpdateApi (PnServiceDeskConfigs pnServiceDeskConfigs) {

        ApiClient newApiClient = new ApiClient(super.initWebClient(ApiClient.buildWebClientBuilder()));
        newApiClient.setBasePath(pnServiceDeskConfigs.getSafeStorageBaseUrl());

        return new FileMetadataUpdateApi(newApiClient);
    }

    @Bean
    public RecipientsApi getRecipientsApi(PnServiceDeskConfigs pnServiceDeskConfigs){
        it.pagopa.pn.service.desk.generated.openapi.msclient.pndatavault.v1.ApiClient apiClient =
                new it.pagopa.pn.service.desk.generated.openapi.msclient.pndatavault.v1.ApiClient(super.initWebClient(it.pagopa.pn.service.desk.generated.openapi.msclient.pndatavault.v1.ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(pnServiceDeskConfigs.getDataVaultBaseUrl());
        return new RecipientsApi(apiClient);
    }

}
