package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.*;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.utility.Utility;
import lombok.CustomLog;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;


@CustomLog
public class PaperChannelMapper {
    private static final String RECEIVER_TYPE = "PF";
    private static final String PRINT_TYPE = "BN_FRONTE_RETRO";

    private PaperChannelMapper(){}

    public static PrepareRequestDto getPrepareRequest (PnServiceDeskOperations operations,
                                                       PnServiceDeskAddress address,
                                                       List<String> attachments,
                                                       String requestId,
                                                       PnServiceDeskConfigs cfn){
        PrepareRequestDto requestDto = new PrepareRequestDto();
        requestDto.setReceiverAddress(AddressMapper.toPreparePaperAddress(address));
        requestDto.setIun(operations.getOperationId());
        ProposalTypeEnumDto proposalProductType = ProposalTypeEnumDto.RS;
        try {
          proposalProductType = ProposalTypeEnumDto.fromValue(cfn.getProductType());
        } catch (IllegalArgumentException ex){
            log.debug("Error mapping product type - from cfn {}", cfn.getProductType());
        }
        requestDto.setProposalProductType(proposalProductType);
        requestDto.setRequestId(requestId);
        requestDto.setPrintType(PRINT_TYPE);
        requestDto.setReceiverFiscalCode(operations.getRecipientInternalId());
        requestDto.setReceiverType(RECEIVER_TYPE);
        requestDto.setAttachmentUrls(attachments);
        return requestDto;
    }

    public static SendRequestDto getPaperSendRequest (PnServiceDeskConfigs pnServiceDeskConfigs, PnServiceDeskOperations operations, PrepareEventDto prepareEventDto) {
        SendRequestDto sendRequestDto = new SendRequestDto();
        sendRequestDto.setRequestPaId(pnServiceDeskConfigs.getSenderPaId());
        sendRequestDto.setReceiverAddress(prepareEventDto.getReceiverAddress());
        sendRequestDto.setIun(Utility.extractOperationId(prepareEventDto.getRequestId()));
        sendRequestDto.setProductType(ProductTypeEnumDto.fromValue(prepareEventDto.getProductType()));
        sendRequestDto.setRequestId(prepareEventDto.getRequestId());
        sendRequestDto.setPrintType(PRINT_TYPE);
        sendRequestDto.setClientRequestTimeStamp(OffsetDateTime.ofInstant(Instant.now(), ZoneOffset.UTC));
        sendRequestDto.setAttachmentUrls(toListStringAttachments(operations));
        sendRequestDto.setSenderAddress(AddressMapper.toAnalogAddressDto(pnServiceDeskConfigs.getSenderAddress()));
        return sendRequestDto;
    }

    private static List<String> toListStringAttachments(PnServiceDeskOperations operations) {
        List<String> attachmentsList = new ArrayList<>();
        for(PnServiceDeskAttachments attachments: operations.getAttachments()) {
            if(Boolean.TRUE.equals(attachments.getIsAvailable())) {
                attachmentsList.addAll(attachments.getFilesKey());
            }
        }
        return attachmentsList;
    }


}
