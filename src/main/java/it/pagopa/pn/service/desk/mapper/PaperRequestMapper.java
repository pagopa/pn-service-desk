package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.AnalogAddressDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.PrepareRequestDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.ProposalTypeEnumDto;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;

import java.util.List;

public class PaperRequestMapper {

    private static final String RECEIVER_TYPE = "PF";

    public static PrepareRequestDto getPrepareRequest (PnServiceDeskOperations operations, PnServiceDeskAddress address, List<String> attachments, String requestId){
        PrepareRequestDto requestDto = new PrepareRequestDto();
        requestDto.setReceiverAddress(getAnalogAddresstoServiceDeskAddress(address));
        requestDto.setIun(operations.getOperationId());
        requestDto.setProposalProductType(ProposalTypeEnumDto.RS);
        requestDto.setRequestId(requestId);
        requestDto.setReceiverFiscalCode(operations.getRecipientInternalId());
        requestDto.setReceiverType(RECEIVER_TYPE);
        requestDto.setAttachmentUrls(attachments);
        return requestDto;
    }

    private static AnalogAddressDto getAnalogAddresstoServiceDeskAddress(PnServiceDeskAddress address){
        AnalogAddressDto analogAddress = new AnalogAddressDto();
        analogAddress.setAddress(address.getAddress());
        analogAddress.setAddressRow2(address.getAddressRow2());
        analogAddress.setCap(address.getCap());
        analogAddress.setCity(address.getCity());
        analogAddress.setCity2(address.getCity2());
        analogAddress.setPr(address.getPr());
        analogAddress.setCountry(address.getCountry());
        return analogAddress;
    }
}
