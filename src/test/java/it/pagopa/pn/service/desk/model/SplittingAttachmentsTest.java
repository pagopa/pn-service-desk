package it.pagopa.pn.service.desk.model;

import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;




public class SplittingAttachmentsTest {

    @Test
    void splitAttachmentTest() {
        final int maxNumber = 100;

        PnServiceDeskAttachments attachment = new PnServiceDeskAttachments();
        attachment.setNumberOfPages(100);
        attachment.setIun("iun1");

        PnServiceDeskAttachments attachment1 = new PnServiceDeskAttachments();
        attachment1.setNumberOfPages(100);
        attachment1.setIun("iun2");

        List<PnServiceDeskAttachments> attachmentsList = new ArrayList<>(List.of(attachment, attachment1));

        PnServiceDeskOperations baseOperation = new PnServiceDeskOperations();
        baseOperation.setOperationId("op1");
        baseOperation.setAttachments(attachmentsList);

        var splittingAttachments = new SplittingAttachments(attachmentsList, baseOperation, maxNumber);
        List<PnServiceDeskOperations> operationsSplittered = splittingAttachments.splitAttachment().collectList().block();

        Assertions.assertNotNull(operationsSplittered);
        Assertions.assertEquals(operationsSplittered.size(), 2);
    }

    @Test
    void splitAttachmentOverMaxNumberOfPagesTest() {
        final int maxNumber = 100;

        PnServiceDeskAttachments attachment = new PnServiceDeskAttachments();
        attachment.setNumberOfPages(150);
        attachment.setIun("iun1");

        PnServiceDeskAttachments attachment1 = new PnServiceDeskAttachments();
        attachment1.setNumberOfPages(180);
        attachment1.setIun("iun2");

        List<PnServiceDeskAttachments> attachmentsList = new ArrayList<>(List.of(attachment, attachment1));

        PnServiceDeskOperations baseOperation = new PnServiceDeskOperations();
        baseOperation.setOperationId("op1");
        baseOperation.setAttachments(attachmentsList);

        var splittingAttachments = new SplittingAttachments(attachmentsList, baseOperation, maxNumber);
        List<PnServiceDeskOperations> operationsSplittered = splittingAttachments.splitAttachment().collectList().block();

        Assertions.assertNotNull(operationsSplittered);
        Assertions.assertEquals(operationsSplittered.size(), 2);
    }
}
