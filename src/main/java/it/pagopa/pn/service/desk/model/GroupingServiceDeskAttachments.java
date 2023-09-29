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

public class GroupingServiceDeskAttachments {

    private List<PnServiceDeskAttachments> source;
    private PnServiceDeskOperations baseOperation;
    private Integer maxNumber;

    public GroupingServiceDeskAttachments(List<PnServiceDeskAttachments> source, PnServiceDeskOperations baseOperation, Integer maxNumber) {
        this.source = source;
        this.baseOperation = baseOperation;
        this.maxNumber = maxNumber;
        this.source.sort(Comparator.comparingInt(PnServiceDeskAttachments::getNumberOfPages).reversed());
    }

    private List<PnServiceDeskAttachments> splitAttachment(List<PnServiceDeskAttachments> attachments, List<PnServiceDeskAttachments> target, int total, int i) {
        if (i == target.size()) return attachments;

        total -= target.get(i).getNumberOfPages();
        if (total < 0) {
            return attachments;
        }

        attachments.add(target.get(i));
        return splitAttachment(attachments, target, total, i+1);
    }

    public Flux<PnServiceDeskOperations> splitAttachment() {
        List<PnServiceDeskAttachments> result = new ArrayList<>();
        List<PnServiceDeskAttachments> temp;
        List<PnServiceDeskOperations> operations = new ArrayList<>();

        result.addAll(source);
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

    public PnServiceDeskOperations createOperationsToSend(PnServiceDeskOperations operations, List<PnServiceDeskAttachments> splitAttachmentsList, String sequenceNumber) {
        PnServiceDeskOperations pnServiceDeskOperations = OperationMapper.copyOperation(operations);
        pnServiceDeskOperations.setAttachments(splitAttachmentsList);
        if (!StringUtils.isEmpty(sequenceNumber)) {
            pnServiceDeskOperations.setOperationId(operations.getOperationId().concat(Const.OPERATION_ID_SUFFIX + sequenceNumber));
        }
        return pnServiceDeskOperations;
    }

}
