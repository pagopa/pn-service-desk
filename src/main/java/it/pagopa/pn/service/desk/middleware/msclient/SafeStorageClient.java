package it.pagopa.pn.service.desk.middleware.msclient;

import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileCreationResponse;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.VideoUploadRequest;
import reactor.core.publisher.Mono;

public interface SafeStorageClient {
    Mono<FileCreationResponse> getPresignedUrl(VideoUploadRequest videoUploadRequest);

}
