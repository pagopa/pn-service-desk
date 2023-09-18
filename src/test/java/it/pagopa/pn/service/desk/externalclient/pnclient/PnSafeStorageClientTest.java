package it.pagopa.pn.service.desk.externalclient.pnclient;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.exception.PnRetryStorageException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileCreationResponse;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.VideoUploadRequest;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.safestorage.PnSafeStorageClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;

class PnSafeStorageClientTest extends BaseTest.WithMockServer {
    private static final String FILE_KEY = "FILE-KEY-ATTACHMENTS";
    private static final String FILE_KEY_RETRY = "FILE-KEY-ATTACHMENTS-RETRY";
    private static final String FILE_KEY_ERROR = "FILE-KEY-ERROR";


    @Autowired
    private PnSafeStorageClient pnSafeStorageClient;

    @Test
    void getPresignedUrl(){
        FileCreationResponse fileCreationResponse = this.pnSafeStorageClient.getPresignedUrl(new VideoUploadRequest()).block();

        Assertions.assertNotNull(fileCreationResponse);

        Assertions.assertEquals(FileCreationResponse.UploadMethodEnum.POST, fileCreationResponse.getUploadMethod());
        Assertions.assertEquals("http://localhost:4566/local-doc-bucket/PN_SERVCDESK_012?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20230628T141853Z&X-Amz-SignedHeaders=host&X-Amz-Expires=359999&X-Amz-Credential=PN-TEST%2F20230628%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Signature=f1a8ae1918ee03bfb8cc0e01f6783e5a47ca34a945211ea726792e98c45a442f", fileCreationResponse.getUploadUrl());
        Assertions.assertEquals("jezIVxlG1M1woCSUngM6KipUN3/p8cG5RMIPnuEanlE=", fileCreationResponse.getSecret());
        Assertions.assertEquals("FILE-KEY-ATTACHMENTS", fileCreationResponse.getKey());

    }

    @Test
    void getFile(){
        FileDownloadResponse fileDownloadResponse = this.pnSafeStorageClient.getFile(FILE_KEY).block();

        Assertions.assertEquals(FILE_KEY, fileDownloadResponse.getKey());
        Assertions.assertEquals("3Z9SdhZ50PBeIj617KEMrztNKDMJj8FZ", fileDownloadResponse.getVersionId());
        Assertions.assertEquals("application/pdf", fileDownloadResponse.getContentType());
        Assertions.assertEquals("jezIVxlG1M1woCSUngM6KipUN3/p8cG5RMIPnuEanlE=", fileDownloadResponse.getChecksum());
        Assertions.assertEquals(OffsetDateTime.parse("2032-04-12T12:32:04Z"), fileDownloadResponse.getRetentionUntil());
        Assertions.assertEquals("PN_LEGALFACT", fileDownloadResponse.getDocumentType());
    }

    @Test
    void getFileWithRetryAfter(){
        StepVerifier.create(pnSafeStorageClient.getFile(FILE_KEY_RETRY))
                        .expectError(PnRetryStorageException.class)
                        .verify();
    }

    @Test
    void getFileErrorResume(){
        StepVerifier.create(pnSafeStorageClient.getFile(FILE_KEY_ERROR))
                .expectError(WebClientResponseException.class)
                .verify();
    }

    @Test
    void getFileWithBaseUrl(){
        FileDownloadResponse fileDownloadResponse = this.pnSafeStorageClient.getFile("safestorage://FILE-KEY-ATTACHMENT").block();

        Assertions.assertEquals("safestorage://FILE-KEY-ATTACHMENT", fileDownloadResponse.getKey());
        Assertions.assertEquals("3Z9SdhZ50PBeIj617KEMrztNKDMJj8FZ", fileDownloadResponse.getVersionId());
        Assertions.assertEquals("application/pdf", fileDownloadResponse.getContentType());
        Assertions.assertEquals("jezIVxlG1M1woCSUngM6KipUN3/p8cG5RMIPnuEanlE=", fileDownloadResponse.getChecksum());
        Assertions.assertEquals(OffsetDateTime.parse("2032-04-12T12:32:04Z"), fileDownloadResponse.getRetentionUntil());
        Assertions.assertEquals("PN_LEGALFACT", fileDownloadResponse.getDocumentType());
    }

}
