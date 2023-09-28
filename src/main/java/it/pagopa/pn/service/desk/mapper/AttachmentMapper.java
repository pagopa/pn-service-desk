package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.model.AttachmentInfo;

import java.util.ArrayList;

import static java.lang.Boolean.TRUE;

public class AttachmentMapper {

    private AttachmentMapper(){}

    public static PnServiceDeskAttachments initAttachment(String iun){
        PnServiceDeskAttachments pnServiceDeskAttachments = new PnServiceDeskAttachments();
        pnServiceDeskAttachments.setIun(iun);
        pnServiceDeskAttachments.setIsAvailable(TRUE);
        pnServiceDeskAttachments.setFilesKey(new ArrayList<>());
        return pnServiceDeskAttachments;
    }

    public static AttachmentInfo fromSafeStorage(FileDownloadResponse response){
        AttachmentInfo info = new AttachmentInfo();
        info.setFileKey(response.getKey());
        if (response.getDownload() != null && response.getDownload().getUrl() != null){
            info.setUrl(response.getDownload().getUrl());
        }
        info.setDocumentType(response.getDocumentType());
        return info;
    }

}
