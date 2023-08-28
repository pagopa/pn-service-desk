package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.safestorage;


import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.exception.PnRetryStorageException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.api.FileDownloadApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.api.FileUploadApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.*;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.VideoUploadRequest;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.safestorage.dto.FileDownloadResponseDto;
import lombok.CustomLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.ConnectException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;


@Component
@Slf4j
@CustomLog
public class PnSafeStorageClientImpl implements PnSafeStorageClient {
    private static final String DOCUMENT_TYPE = "PN_SERVICEDESK_RECORDING";
    private static final String CHECKSUM = "SHA256";
    private static final String STATUS = "PRELOADED";

    @Autowired
    private PnServiceDeskConfigs pnServiceDeskConfig;
    @Autowired
    private FileUploadApi fileUploadApi;
    @Autowired
    private FileDownloadApi fileDownloadApi;

    private PnServiceDeskConfigs pnServiceDeskConfigs;


    @Override
    public Mono<FileCreationResponse> getPresignedUrl(VideoUploadRequest videoUploadRequest) {
        FileCreationRequest fileCreationRequest = new FileCreationRequest();
        fileCreationRequest.setContentType(videoUploadRequest.getContentType());
        fileCreationRequest.setDocumentType(DOCUMENT_TYPE);
        fileCreationRequest.setStatus(STATUS);

        return fileUploadApi.createFile(this.pnServiceDeskConfig.getSafeStorageCxId(), CHECKSUM, videoUploadRequest.getSha256(), fileCreationRequest);
    }

    @Override
    public Mono<FileDownloadResponse> getFile(String fileKey) {
        String PN_SAFE_STORAGE_DESCRIPTION = "Safe Storage getFile";
        //og.logInvokingAsyncExternalService(PnLogger.EXTERNAL_SERVICES.PN_SAFE_STORAGE, PN_SAFE_STORAGE_DESCRIPTION, null);
        String reqFileKey = fileKey;
        log.info("Getting file with {} key", fileKey);
        String BASE_URL = "safestorage://";
        if (fileKey.contains(BASE_URL)){
            fileKey = fileKey.replace(BASE_URL, "");
        }
        log.debug("Req params : {}", fileKey);

        return fileDownloadApi.getFile(fileKey, this.pnServiceDeskConfigs.getSafeStorageCxId(), false)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(500))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                )
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
