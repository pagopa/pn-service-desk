package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.safestorage;

import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileCreationResponse;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.VideoUploadRequest;
import reactor.core.publisher.Mono;

public interface PnSafeStorageClient {
    String CLIENT_NAME = PnLogger.EXTERNAL_SERVICES.PN_SAFE_STORAGE;

    Mono<FileCreationResponse> getPresignedUrl(VideoUploadRequest videoUploadRequest);



}
