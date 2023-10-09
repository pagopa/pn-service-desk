package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileDownloadInfo;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttachmentMapperTest {

    @Test
    void initAttachment() {
        assertNotNull(AttachmentMapper.initAttachment("1234"));
    }

    @Test
    void fromSafeStorage() {
        FileDownloadResponse response = new FileDownloadResponse();
        response.setKey("http:safeStorage/12345");
        response.setChecksum("SHA256");
        response.setContentType("application/pdf");
        response.setDocumentType("PN_NOTIFICATION_ATTACHMENTS");
        FileDownloadInfo downloadInfo = new FileDownloadInfo();
        downloadInfo.setUrl("https:download");
        response.setDownload(downloadInfo);
        assertNotNull(AttachmentMapper.fromSafeStorage(response));

        downloadInfo.setUrl(null);
        response.setDownload(downloadInfo);
        assertNull(AttachmentMapper.fromSafeStorage(response).getUrl());
    }
}