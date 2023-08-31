package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.PrepareEventDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.ProductTypeEnumDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.SendRequestDto;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.utility.Utility;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class PaperSendMapper {

    public static SendRequestDto getPaperSendRequest (PnServiceDeskConfigs pnServiceDeskConfigs, PnServiceDeskOperations operations, PrepareEventDto prepareEventDto) {
        SendRequestDto sendRequestDto = new SendRequestDto();
        sendRequestDto.setRequestPaId(pnServiceDeskConfigs.getSenderPaId());
        sendRequestDto.setReceiverAddress(prepareEventDto.getReceiverAddress());
        sendRequestDto.setIun(Utility.extractOperationId(prepareEventDto.getRequestId()));
        sendRequestDto.setProductType(ProductTypeEnumDto.fromValue(prepareEventDto.getProductType()));
        sendRequestDto.setRequestId(prepareEventDto.getRequestId());
        sendRequestDto.setPrintType("BN_FRONTE_RETRO");
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
