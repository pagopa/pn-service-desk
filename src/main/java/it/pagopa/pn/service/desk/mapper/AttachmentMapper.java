package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;

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

}
