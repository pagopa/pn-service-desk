package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.safestorage;


import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.exception.PnRetryStorageException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.api.FileDownloadApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.api.FileUploadApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.*;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.VideoUploadRequest;
import it.pagopa.pn.service.desk.utility.Utility;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_SAFE_STORAGE_BODY_NULL;


@Component
@Slf4j
@CustomLog
@AllArgsConstructor
public class PnSafeStorageClientImpl implements PnSafeStorageClient {
    private static final String CHECKSUM = "SHA256";
    private static final String STATUS_SAVED = "SAVED";

    private PnServiceDeskConfigs pnServiceDeskConfig;
    private FileUploadApi fileUploadApi;
    private FileDownloadApi fileDownloadApi;


    @Override
    public Mono<FileCreationResponse> getPresignedUrl(VideoUploadRequest videoUploadRequest) {
        FileCreationRequest fileCreationRequest = new FileCreationRequest();
        fileCreationRequest.setContentType(videoUploadRequest.getContentType());
        fileCreationRequest.setDocumentType(pnServiceDeskConfig.getSafeStorageDocumentType());
        fileCreationRequest.setStatus(STATUS_SAVED);

        return fileUploadApi.createFile(this.pnServiceDeskConfig.getSafeStorageCxId(), CHECKSUM, videoUploadRequest.getSha256(), fileCreationRequest);
    }

    @Override
    public Mono<FileDownloadResponse> getFile(String fileKey) {
        final String PN_SAFE_STORAGE_DESCRIPTION = "Safe Storage getFile";
        log.debug(PN_SAFE_STORAGE_DESCRIPTION);
        String reqFileKey = fileKey;
        log.info("Getting file with {} key", fileKey);
        final String BASE_URL = Utility.SAFESTORAGE_BASE_URL;
        if (fileKey.contains(BASE_URL)){
            fileKey = fileKey.replace(BASE_URL, "");
        }
        log.debug("Req params : {}", fileKey);

        return fileDownloadApi.getFile(fileKey, this.pnServiceDeskConfig.getSafeStorageCxId(), false)
                .switchIfEmpty(Mono.error(new PnGenericException(ERROR_SAFE_STORAGE_BODY_NULL, ERROR_SAFE_STORAGE_BODY_NULL.getMessage().concat(fileKey))))
                .map(response -> {
                    if(response.getDownload() != null && response.getDownload().getRetryAfter() != null) {
                        throw new PnRetryStorageException(response.getDownload().getRetryAfter());
                    }
                    response.setKey(reqFileKey);
                    return response;
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error(ex.getResponseBodyAsString());
                    return Mono.error(ex);
                });
    }

}
