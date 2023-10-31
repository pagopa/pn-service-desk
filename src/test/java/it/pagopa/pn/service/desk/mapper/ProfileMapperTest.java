package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pnmandate.v1.dto.InternalMandateDtoDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnuserattributes.v1.dto.*;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.Address;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.Mandate;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.ProfileResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProfileMapperTest {

    @Test
    void getAddress(){
        ProfileResponse profileResponse = new ProfileResponse();

        List<LegalDigitalAddressDto> legalDigitalAddressDtoList = new ArrayList<>();
        LegalDigitalAddressDto legalDigitalAddressDto = new LegalDigitalAddressDto();
        legalDigitalAddressDto.setValue("example@pecSuccess.it");
        legalDigitalAddressDto.setAddressType(LegalAddressTypeDto.LEGAL);
        legalDigitalAddressDto.setChannelType(LegalChannelTypeDto.PEC);
        legalDigitalAddressDto.setSenderId("default");
        legalDigitalAddressDto.setRecipientId("PF-4fc75df3-0913-407e-bdaa-e50329708b7d");
        legalDigitalAddressDtoList.add(legalDigitalAddressDto);

        List<CourtesyDigitalAddressDto> courtesyDigitalAddressDtoList = new ArrayList<>();
        CourtesyDigitalAddressDto courtesyDigitalAddressDto = new CourtesyDigitalAddressDto();
        courtesyDigitalAddressDto.setValue("example@pecSuccess.it");
        courtesyDigitalAddressDto.setAddressType(CourtesyAddressTypeDto.COURTESY);
        courtesyDigitalAddressDto.setChannelType(CourtesyChannelTypeDto.SMS);
        courtesyDigitalAddressDto.setSenderId("default");
        courtesyDigitalAddressDto.setRecipientId("PF-4fc75df3-0913-407e-bdaa-e50329708b7d");
        courtesyDigitalAddressDtoList.add(courtesyDigitalAddressDto);

        profileResponse = ProfileMapper.getAddress(legalDigitalAddressDtoList, courtesyDigitalAddressDtoList, profileResponse);

        Address actualLegalAddress = profileResponse.getUserAddresses().get(0);
        Address actualCourtesyAddress = profileResponse.getUserAddresses().get(1);

        Assertions.assertEquals(legalDigitalAddressDto.getValue(), actualLegalAddress.getLegalValue());
        Assertions.assertEquals(legalDigitalAddressDto.getChannelType().getValue(), actualLegalAddress.getLegalChannelType().getValue());
        Assertions.assertEquals(legalDigitalAddressDto.getAddressType().getValue(), actualLegalAddress.getLegalAddressType().getValue());

        Assertions.assertEquals(courtesyDigitalAddressDto.getValue(), actualCourtesyAddress.getCourtesyValue());
        Assertions.assertEquals(courtesyDigitalAddressDto.getChannelType().getValue(), actualCourtesyAddress.getCourtesyChannelType().getValue());
        Assertions.assertEquals(courtesyDigitalAddressDto.getAddressType().getValue(), actualCourtesyAddress.getCourtesyAddressType().getValue());
    }

    @Test
    void getMandate(){
        ProfileResponse profileResponse = new ProfileResponse();

        List<InternalMandateDtoDto> internalDelegatorDtoDtoList = new ArrayList<>();
        InternalMandateDtoDto internalDelegatorDtoDto = new InternalMandateDtoDto();
        internalDelegatorDtoDto.setDatefrom("2023-06-18T22:00:00Z");
        internalDelegatorDtoDto.setDateto("2023-10-18T21:59:59Z");
        internalDelegatorDtoDto.setMandateId("49258827-a23d-4712-a46f-e23a67b4150f");
        internalDelegatorDtoDto.setDelegate("PF-37654561-446a-4c88-b328-6699a8322b33");
        internalDelegatorDtoDto.setDelegator("PF-4fc75df3-0913-407e-bdaa-e50329708b7d");
        internalDelegatorDtoDtoList.add(internalDelegatorDtoDto);

        List<InternalMandateDtoDto> internalDelegateDtoDtoList = new ArrayList<>();
        InternalMandateDtoDto internalDelegateDtoDto = new InternalMandateDtoDto();
        internalDelegateDtoDto.setDatefrom("2023-01-10T23:00:00Z");
        internalDelegateDtoDto.setDateto("2026-05-12T21:59:59Z");
        internalDelegateDtoDto.setMandateId("64270e52-b5d5-4cf3-bbe2-15e8ac750107");
        internalDelegateDtoDto.setDelegate("PF-4fc75df3-0913-407e-bdaa-e50329708b7d");
        internalDelegateDtoDto.setDelegator("PG-8f409ca6-fc3d-46c3-8da7-69cd9a1e9e62");
        internalDelegateDtoDtoList.add(internalDelegateDtoDto);

        profileResponse = ProfileMapper.getMandate(internalDelegatorDtoDtoList, internalDelegateDtoDtoList, profileResponse);

        Mandate actualDelegator = profileResponse.getDelegatorMandates().get(0);
        Mandate actualDelegate = profileResponse.getDelegateMandates().get(0);

        Assertions.assertEquals(actualDelegator.getDateFrom(), OffsetDateTime.parse(internalDelegatorDtoDto.getDatefrom()));
        Assertions.assertEquals(actualDelegator.getDateTo(), OffsetDateTime.parse(internalDelegatorDtoDto.getDateto()));
        Assertions.assertEquals(actualDelegator.getMandateId(), internalDelegatorDtoDto.getMandateId());
        Assertions.assertEquals(actualDelegator.getDelegateInternalId(), internalDelegatorDtoDto.getDelegate());
        Assertions.assertEquals(actualDelegator.getDelegatorInternalId(), internalDelegatorDtoDto.getDelegator());

        Assertions.assertEquals(actualDelegate.getDateFrom(), OffsetDateTime.parse(internalDelegateDtoDto.getDatefrom()));
        Assertions.assertEquals(actualDelegate.getDateTo(), OffsetDateTime.parse(internalDelegateDtoDto.getDateto()));
        Assertions.assertEquals(actualDelegate.getMandateId(), internalDelegateDtoDto.getMandateId());
        Assertions.assertEquals(actualDelegate.getDelegateInternalId(), null);
        Assertions.assertEquals(actualDelegate.getDelegatorInternalId(), internalDelegateDtoDto.getDelegate());
    }

}