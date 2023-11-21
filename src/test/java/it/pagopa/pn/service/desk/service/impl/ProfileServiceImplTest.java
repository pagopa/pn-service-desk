package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnmandate.v1.dto.InternalMandateDtoDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnuserattributes.v1.dto.*;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault.PnDataVaultClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.mandate.MandateClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.userattributes.PnUserAttributesClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

class ProfileServiceImplTest extends BaseTest {

    @MockBean
    private PnDataVaultClient dataVaultClient;
    @MockBean
    private PnUserAttributesClient userAttributesClient;
    @MockBean
    private MandateClient mandateClient;
    @Autowired
    private ProfileServiceImpl profileService;

    @Test
    void getProfileFromTaxId(){
        Mockito.when(this.dataVaultClient.anonymized(Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just("PF-4fc75df3-0913-407e-bdaa-e50329708b7d"));
        Mockito.when(this.userAttributesClient.getLegalAddressBySender(Mockito.any(),Mockito.any()))
                .thenReturn(Flux.fromIterable(getLegalDigitalAddressDto()));
        Mockito.when(this.userAttributesClient.getCourtesyAddressBySender(Mockito.any(),Mockito.any()))
                .thenReturn(Flux.fromIterable(getCourtesyDigitalAddressDto()));
        Mockito.when(this.mandateClient.listMandatesByDelegator(Mockito.any()))
                .thenReturn(Flux.fromIterable(getDelegator()));
        Mockito.when(this.mandateClient.listMandatesByDelegate(Mockito.any()))
                .thenReturn(Flux.fromIterable(getDelegate()));
        Mockito.when(this.dataVaultClient.deAnonymized(Mockito.any())).thenReturn(Mono.just("MDDLOP3333-e"));

        ProfileResponse actualResponse = this.profileService.getProfileFromTaxId("fkdokm", getProfileRequest()).block();

        Assertions.assertNotNull(actualResponse);

        Mandate mandate = actualResponse.getDelegateMandates().get(0);
        Mandate delegate = actualResponse.getDelegatorMandates().get(0);
        Address userLegalAddress = actualResponse.getUserAddresses().get(0);
        Address userCourtesyAddress = actualResponse.getUserAddresses().get(1);

        Assertions.assertEquals("64270e52-b5d5-4cf3-bbe2-15e8ac750107", mandate.getMandateId());
        Assertions.assertEquals(OffsetDateTime.parse("2023-01-10T23:00Z"), mandate.getDateFrom());
        Assertions.assertEquals(OffsetDateTime.parse("2026-05-12T21:59:59Z"), mandate.getDateTo());
        Assertions.assertEquals("PF-4fc75df3-0913-407e-bdaa-e50329708b7d", mandate.getDelegatorInternalId());
        Assertions.assertNull(mandate.getDelegateInternalId());
        Assertions.assertEquals(RecipientType.PG.getValue(),mandate.getRecipientType().getValue());

        Assertions.assertEquals("49258827-a23d-4712-a46f-e23a67b4150f", delegate.getMandateId());
        Assertions.assertEquals(OffsetDateTime.parse("2023-06-18T22:00:00Z"), delegate.getDateFrom());
        Assertions.assertEquals(OffsetDateTime.parse("2023-10-18T21:59:59Z"), delegate.getDateTo());
        Assertions.assertEquals("PF-4fc75df3-0913-407e-bdaa-e50329708b7d", delegate.getDelegatorInternalId());
        Assertions.assertEquals("PF-37654561-446a-4c88-b328-6699a8322b33", delegate.getDelegateInternalId());
        Assertions.assertEquals("MDDLOP3333-e", delegate.getTaxId());
        Assertions.assertEquals(RecipientType.PF.getValue(),delegate.getRecipientType().getValue());

        Assertions.assertEquals(LegalAddressType.LEGAL.getValue(), userLegalAddress.getLegalAddressType().getValue());
        Assertions.assertEquals(LegalChannelType.PEC.getValue(), userLegalAddress.getLegalChannelType().getValue());
        Assertions.assertEquals("example@pecSuccess.it", userLegalAddress.getLegalValue());

        Assertions.assertEquals(CourtesyAddressType.COURTESY.getValue(), userCourtesyAddress.getCourtesyAddressType().getValue());
        Assertions.assertEquals(CourtesyChannelType.SMS.getValue(), userCourtesyAddress.getCourtesyChannelType().getValue());
        Assertions.assertEquals("example@pecSuccess.it", userCourtesyAddress.getCourtesyValue());
    }

    private List<InternalMandateDtoDto> getDelegator() {
        List<InternalMandateDtoDto> internalMandateDtoDtoList = new ArrayList<>();
        InternalMandateDtoDto internalMandateDtoDto = new InternalMandateDtoDto();
        internalMandateDtoDto.setDatefrom("2023-06-18T22:00:00Z");
        internalMandateDtoDto.setDateto("2023-10-18T21:59:59Z");
        internalMandateDtoDto.setMandateId("49258827-a23d-4712-a46f-e23a67b4150f");
        internalMandateDtoDto.setDelegate("PF-37654561-446a-4c88-b328-6699a8322b33");
        internalMandateDtoDto.setDelegator("PF-4fc75df3-0913-407e-bdaa-e50329708b7d");
        List<String> visibilityIds = new ArrayList<>();
        visibilityIds.add("026e8c72-7944-4dcd-8668-f596447fec6d");
        internalMandateDtoDto.setVisibilityIds(visibilityIds);
        internalMandateDtoDtoList.add(internalMandateDtoDto);
        return internalMandateDtoDtoList;
    }

    private List<InternalMandateDtoDto> getDelegate() {
        List<InternalMandateDtoDto> internalMandateDtoDtoList = new ArrayList<>();
        InternalMandateDtoDto internalMandateDtoDto = new InternalMandateDtoDto();
        internalMandateDtoDto.setDatefrom("2023-01-10T23:00:00Z");
        internalMandateDtoDto.setDateto("2026-05-12T21:59:59Z");
        internalMandateDtoDto.setMandateId("64270e52-b5d5-4cf3-bbe2-15e8ac750107");
        internalMandateDtoDto.setDelegate("PF-4fc75df3-0913-407e-bdaa-e50329708b7d");
        internalMandateDtoDto.setDelegator("PG-8f409ca6-fc3d-46c3-8da7-69cd9a1e9e62");
        List<String> groups = new ArrayList<>();
        groups.add("645cf38b2030541ce50153b5");
        internalMandateDtoDto.setGroups(groups);
        List<String> visibilityIds = new ArrayList<>();
        visibilityIds.add("026e8c72-7944-4dcd-8668-f596447fec6d");
        internalMandateDtoDto.setVisibilityIds(visibilityIds);
        internalMandateDtoDtoList.add(internalMandateDtoDto);
        return internalMandateDtoDtoList;
    }

    private List<CourtesyDigitalAddressDto> getCourtesyDigitalAddressDto() {
        List<CourtesyDigitalAddressDto> courtesyDigitalAddressDtoList = new ArrayList<>();
        CourtesyDigitalAddressDto courtesyDigitalAddressDto = new CourtesyDigitalAddressDto();
        courtesyDigitalAddressDto.setValue("example@pecSuccess.it");
        courtesyDigitalAddressDto.setAddressType(CourtesyAddressTypeDto.COURTESY);
        courtesyDigitalAddressDto.setChannelType(CourtesyChannelTypeDto.SMS);
        courtesyDigitalAddressDto.setSenderId("default");
        courtesyDigitalAddressDto.setRecipientId("PF-4fc75df3-0913-407e-bdaa-e50329708b7d");
        courtesyDigitalAddressDtoList.add(courtesyDigitalAddressDto);
        return courtesyDigitalAddressDtoList;
    }

    private List<LegalDigitalAddressDto> getLegalDigitalAddressDto() {
        List<LegalDigitalAddressDto> legalDigitalAddressDtoList = new ArrayList<>();
        LegalDigitalAddressDto legalDigitalAddressDto = new LegalDigitalAddressDto();
        legalDigitalAddressDto.setValue("example@pecSuccess.it");
        legalDigitalAddressDto.setAddressType(LegalAddressTypeDto.LEGAL);
        legalDigitalAddressDto.setChannelType(LegalChannelTypeDto.PEC);
        legalDigitalAddressDto.setSenderId("default");
        legalDigitalAddressDto.setRecipientId("PF-4fc75df3-0913-407e-bdaa-e50329708b7d");
        legalDigitalAddressDtoList.add(legalDigitalAddressDto);
        return legalDigitalAddressDtoList;
    }

    private ProfileRequest getProfileRequest() {
        ProfileRequest profileRequest = new ProfileRequest();
        profileRequest.setTaxId("PF-4fc75df3-0913-407e-bdaa-e50329708b7d");
        profileRequest.setRecipientType(RecipientType.PF);
        return profileRequest;
    }

}
