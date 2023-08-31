package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.AnalogAddressDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.PrepareRequestDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.ProposalTypeEnumDto;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import lombok.CustomLog;

import java.util.List;


@CustomLog
public class PaperRequestMapper {

    private static final String RECEIVER_TYPE = "PF";

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
        requestDto.setReceiverFiscalCode(operations.getRecipientInternalId());
        requestDto.setReceiverType(RECEIVER_TYPE);
        requestDto.setAttachmentUrls(attachments);
        return requestDto;
    }


}
