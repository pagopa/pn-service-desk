package it.pagopa.pn.service.desk.middleware.msclient.impl;


import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.api.FileUploadApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.*;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.VideoUploadRequest;
import it.pagopa.pn.service.desk.middleware.msclient.SafeStorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;



@Component
@Slf4j
public class SafeStorageClientImpl implements SafeStorageClient {
    private final PnServiceDeskConfigs pnServiceDeskConfig;
    private final FileUploadApi fileUploadApi;

    private static final String DOCUMENT_TYPE = "PN_SERVICEDESK_RECORDING";
    private static final String CHECKSUM = "SHA256";
    private static final String STATUS = "PRELOADED";

    public SafeStorageClientImpl(PnServiceDeskConfigs cfg,
                                 FileUploadApi fileUploadApi) {
        this.pnServiceDeskConfig = cfg;
        this.fileUploadApi = fileUploadApi;
    }


    @Override
    public Mono<FileCreationResponse> getPresignedUrl(VideoUploadRequest videoUploadRequest) {
        FileCreationRequest fileCreationRequest = new FileCreationRequest();
        fileCreationRequest.setContentType(videoUploadRequest.getContentType());
        fileCreationRequest.setDocumentType(DOCUMENT_TYPE);
        fileCreationRequest.setStatus(STATUS);

        return fileUploadApi.createFile(this.pnServiceDeskConfig.getSafeStorageCxId(), CHECKSUM, videoUploadRequest.getSha256(), fileCreationRequest);
    }

}
