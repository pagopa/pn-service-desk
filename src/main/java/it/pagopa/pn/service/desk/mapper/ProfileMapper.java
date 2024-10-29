package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pnmandate.v1.dto.InternalMandateDtoDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnuserattributes.v1.dto.CourtesyDigitalAddressDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnuserattributes.v1.dto.LegalDigitalAddressDto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ProfileMapper {

    private ProfileMapper(){}

    public static ProfileResponse getAddress (List<LegalDigitalAddressDto> legalDigitalAddress, List<CourtesyDigitalAddressDto> courtesyDigitalAddress, ProfileResponse response){
        List<Address> addressList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(legalDigitalAddress)){
            legalDigitalAddress.forEach(legalDigitalAddressDto -> {
                Address legalAddress = new Address();
                legalAddress.setLegalAddressType(LegalAddressType.fromValue(legalDigitalAddressDto.getAddressType().getValue()));
                legalAddress.setLegalChannelType(LegalChannelType.fromValue(legalDigitalAddressDto.getChannelType().getValue()));
                legalAddress.setLegalValue(legalDigitalAddressDto.getValue());
                addressList.add(legalAddress);
            });
        }
        if (!CollectionUtils.isEmpty(courtesyDigitalAddress)){
            courtesyDigitalAddress.forEach(courtesyDigitalAddressDto -> {
                Address courtesyAddress = new Address();
                courtesyAddress.setCourtesyAddressType(CourtesyAddressType.fromValue(courtesyDigitalAddressDto.getAddressType().getValue()));
                courtesyAddress.setCourtesyChannelType(CourtesyChannelType.fromValue(courtesyDigitalAddressDto.getChannelType().getValue()));
                courtesyAddress.setCourtesyValue(courtesyDigitalAddressDto.getValue());
                addressList.add(courtesyAddress);
            });
        }

        response.setUserAddresses(addressList);
        return response;
    }

    public static ProfileResponse getMandate (List<InternalMandateDtoDto> internalMandateDelegators, List<InternalMandateDtoDto> internalMandateDelegates, ProfileResponse response){
        List<Mandate> delegatorMandates = new ArrayList<>();
        List<Mandate> delegateMandates = new ArrayList<>();

        if (!internalMandateDelegators.isEmpty()){
            internalMandateDelegators.forEach(internalMandateDto -> {
                Mandate mandate = new Mandate();
                mandate.setMandateId(internalMandateDto.getMandateId());
                mandate.setDateFrom(Instant.parse(internalMandateDto.getDatefrom()));
                mandate.setDateTo(Instant.parse(internalMandateDto.getDateto()));
                mandate.setDelegatorInternalId(internalMandateDto.getDelegator());
                mandate.setRecipientType(RecipientType.fromValue(internalMandateDto.getDelegator().split("-")[0]));
                mandate.setDelegateInternalId(internalMandateDto.getDelegate());
                delegatorMandates.add(mandate);
            });
        }
        response.setDelegatorMandates(delegatorMandates);

        if (!internalMandateDelegates.isEmpty()){
            internalMandateDelegates.forEach(internalMandateDto -> {
                Mandate mandate = new Mandate();
                mandate.setMandateId(internalMandateDto.getMandateId());
                mandate.setDateFrom(Instant.parse(internalMandateDto.getDatefrom()));
                mandate.setDateTo(Instant.parse(internalMandateDto.getDateto()));
                mandate.setDelegatorInternalId(internalMandateDto.getDelegator());
                mandate.setDelegateInternalId(internalMandateDto.getDelegate());
                mandate.setRecipientType(RecipientType.fromValue(internalMandateDto.getDelegate().split("-")[0]));
                delegateMandates.add(mandate);
            });
        }
        response.setDelegateMandates(delegateMandates);

        return response;

    }
}
