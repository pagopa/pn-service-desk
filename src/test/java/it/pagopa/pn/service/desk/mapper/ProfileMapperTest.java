package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pnmandate.v1.dto.InternalMandateDtoDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnuserattributes.v1.dto.*;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.Address;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.Mandate;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.ProfileResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

class ProfileMapperTest {

    @Test
    void getAddress(){
        ProfileResponse profileResponse = new ProfileResponse();

        List<LegalDigitalAddressDto> legalDigitalAddressDtoList = new ArrayList<>();
        LegalDigitalAddressDto legalDigitalAddressDto = new LegalDigitalAddressDto();
        legalDigitalAddressDto.setValue("example@pecSuccess.it");
        legalDigitalAddressDto.setAddressType(LegalAddressTypeDto.LEGAL);
        legalDigitalAddressDto.setChannelType(LegalChannelTypeDto.PEC);
        legalDigitalAddressDto.setSenderId("default");
        legalDigitalAddressDto.setRecipientId("PF-1234");
        legalDigitalAddressDtoList.add(legalDigitalAddressDto);
        legalDigitalAddressDto.setCreated(Instant.now());
        legalDigitalAddressDto.setLastModified(Instant.now());

        List<CourtesyDigitalAddressDto> courtesyDigitalAddressDtoList = new ArrayList<>();
        CourtesyDigitalAddressDto courtesyDigitalAddressDto = new CourtesyDigitalAddressDto();
        courtesyDigitalAddressDto.setValue("example@pecSuccess.it");
        courtesyDigitalAddressDto.setAddressType(CourtesyAddressTypeDto.COURTESY);
        courtesyDigitalAddressDto.setChannelType(CourtesyChannelTypeDto.SMS);
        courtesyDigitalAddressDto.setSenderId("default");
        courtesyDigitalAddressDto.setRecipientId("PF-1234");
        courtesyDigitalAddressDto.setCreated(Instant.now());
        courtesyDigitalAddressDto.setLastModified(Instant.now());
        courtesyDigitalAddressDtoList.add(courtesyDigitalAddressDto);

        profileResponse = ProfileMapper.getAddress(legalDigitalAddressDtoList, courtesyDigitalAddressDtoList, profileResponse);

        Address actualLegalAddress = profileResponse.getUserAddresses().get(0);
        Address actualCourtesyAddress = profileResponse.getUserAddresses().get(1);

        Assertions.assertEquals(legalDigitalAddressDto.getValue(), actualLegalAddress.getLegalValue());
        Assertions.assertEquals(legalDigitalAddressDto.getChannelType().getValue(), actualLegalAddress.getLegalChannelType().getValue());
        Assertions.assertEquals(legalDigitalAddressDto.getAddressType().getValue(), actualLegalAddress.getLegalAddressType().getValue());
        Assertions.assertEquals(legalDigitalAddressDto.getCreated(), actualLegalAddress.getCreated());
        Assertions.assertEquals(legalDigitalAddressDto.getLastModified(), actualLegalAddress.getLastModified());

        Assertions.assertEquals(courtesyDigitalAddressDto.getValue(), actualCourtesyAddress.getCourtesyValue());
        Assertions.assertEquals(courtesyDigitalAddressDto.getChannelType().getValue(), actualCourtesyAddress.getCourtesyChannelType().getValue());
        Assertions.assertEquals(courtesyDigitalAddressDto.getAddressType().getValue(), actualCourtesyAddress.getCourtesyAddressType().getValue());
        Assertions.assertEquals(courtesyDigitalAddressDto.getCreated(), actualCourtesyAddress.getCreated());
        Assertions.assertEquals(courtesyDigitalAddressDto.getLastModified(), actualCourtesyAddress.getLastModified());

    }

    @Test
    void getMandate(){
        ProfileResponse profileResponse = new ProfileResponse();
        List<InternalMandateDtoDto> internalDelegatorDtoDtoList = new ArrayList<>();
        InternalMandateDtoDto internalDelegatorDtoDto = new InternalMandateDtoDto();
        internalDelegatorDtoDto.setDatefrom("2023-06-18T22:00:00Z");
        internalDelegatorDtoDto.setDateto("2023-10-18T21:59:59Z");
        internalDelegatorDtoDto.setMandateId("123-456");
        internalDelegatorDtoDto.setDelegate("PF-00000012");
        internalDelegatorDtoDto.setDelegator("PF-1234");
        internalDelegatorDtoDtoList.add(internalDelegatorDtoDto);

        List<InternalMandateDtoDto> internalDelegateDtoDtoList = new ArrayList<>();
        InternalMandateDtoDto internalDelegateDtoDto = new InternalMandateDtoDto();
        internalDelegateDtoDto.setDatefrom("2023-01-10T23:00:00Z");
        internalDelegateDtoDto.setDateto("2026-05-12T21:59:59Z");
        internalDelegateDtoDto.setMandateId("9999-b5d5-4cf3-bbe2");
        internalDelegateDtoDto.setDelegate("PF-1234");
        internalDelegateDtoDto.setDelegator("PG-0000-1111");
        internalDelegateDtoDtoList.add(internalDelegateDtoDto);

        profileResponse = ProfileMapper.getMandate(internalDelegatorDtoDtoList, internalDelegateDtoDtoList, profileResponse);

        Mandate actualDelegator = profileResponse.getDelegatorMandates().get(0);
        Mandate actualDelegate = profileResponse.getDelegateMandates().get(0);

        Assertions.assertEquals(actualDelegator.getDateFrom(), Instant.parse(internalDelegatorDtoDto.getDatefrom()));
        Assertions.assertEquals(actualDelegator.getDateTo(), Instant.parse(internalDelegatorDtoDto.getDateto()));
        Assertions.assertEquals(actualDelegator.getMandateId(), internalDelegatorDtoDto.getMandateId());
        Assertions.assertEquals(actualDelegator.getDelegateInternalId(), internalDelegatorDtoDto.getDelegate());
        Assertions.assertEquals(actualDelegator.getDelegatorInternalId(), internalDelegatorDtoDto.getDelegator());

        Assertions.assertEquals(actualDelegate.getDateFrom(), Instant.parse(internalDelegateDtoDto.getDatefrom()));
        Assertions.assertEquals(actualDelegate.getDateTo(), Instant.parse(internalDelegateDtoDto.getDateto()));
        Assertions.assertEquals(actualDelegate.getMandateId(), internalDelegateDtoDto.getMandateId());
        Assertions.assertEquals(actualDelegator.getDelegatorInternalId(), internalDelegateDtoDto.getDelegate());
        Assertions.assertEquals(actualDelegate.getDelegateInternalId(), internalDelegateDtoDto.getDelegate());
    }

}