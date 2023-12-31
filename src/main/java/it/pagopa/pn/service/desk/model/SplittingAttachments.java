package it.pagopa.pn.service.desk.model;

import it.pagopa.pn.service.desk.mapper.OperationMapper;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.utility.Const;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SplittingAttachments {

    private List<PnServiceDeskAttachments> source;
    private PnServiceDeskOperations baseOperation;
    private Integer maxNumber;

    public SplittingAttachments(List<PnServiceDeskAttachments> source, PnServiceDeskOperations baseOperation, Integer maxNumber) {
        this.source = source;
        this.baseOperation = baseOperation;
        this.maxNumber = maxNumber;
        this.source.sort(Comparator.comparingInt(PnServiceDeskAttachments::getNumberOfPages).reversed());
    }

    /**
     * Fills the list as long as the maximum number of pages is not exceeded
     * @param attachments elements to add in the result
     * @param target current list
     * @param total current number of pages into list
     * @param i index
     * @return list of attachments
     */
    private List<PnServiceDeskAttachments> splitAttachment(List<PnServiceDeskAttachments> attachments, List<PnServiceDeskAttachments> target, int total, int i) {
        if (i == target.size()) return attachments;
        if (target.get(i).getNumberOfPages() > maxNumber) {
            attachments.add(target.get(i));
            return attachments;
        }

        total -= target.get(i).getNumberOfPages();
        if (total < 0) {
            return attachments;
        }

        attachments.add(target.get(i));
        return splitAttachment(attachments, target, total, i+1);
    }

    /**
     * Creates a stream of PnServiceDeskOperations
     * Divide attachments according to their number of pages
     * If the list of attachments exceeds the maximum number of pages, then creates a new PnServiceDeskOperations
     * @return list of attachments
     */
    public Flux<PnServiceDeskOperations> splitAttachment() {
        List<PnServiceDeskAttachments> temp;
        List<PnServiceDeskOperations> operations = new ArrayList<>();
        List<PnServiceDeskAttachments> result = new ArrayList<>(source);
        int count = 0;
        while(!result.isEmpty()) {
            temp = splitAttachment(new ArrayList<>(), result, this.maxNumber, 0);
            result.removeAll(temp);
            PnServiceDeskOperations op = createOperationsToSend(this.baseOperation, temp, (count == 0 ? "" : String.valueOf(count)));
            operations.add(op);
            count++;
        }
        return Flux.fromIterable(operations);
    }

    /**
     * Creates a PnServiceDeskOperations
     * Adds suffix in operationId
     * @return an operation
     */
    public PnServiceDeskOperations createOperationsToSend(PnServiceDeskOperations operations, List<PnServiceDeskAttachments> splitAttachmentsList, String sequenceNumber) {
        PnServiceDeskOperations pnServiceDeskOperations = OperationMapper.copyOperation(operations);
        pnServiceDeskOperations.setAttachments(splitAttachmentsList);
        if (!StringUtils.isEmpty(sequenceNumber)) {
            pnServiceDeskOperations.setOperationId(operations.getOperationId().concat(Const.OPERATION_ID_SUFFIX + sequenceNumber));
        }
        return pnServiceDeskOperations;
    }

}
