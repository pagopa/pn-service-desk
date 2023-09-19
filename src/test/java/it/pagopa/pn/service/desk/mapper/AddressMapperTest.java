package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnaddressmanager.v1.dto.AnalogAddressDto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.AnalogAddress;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class AddressMapperTest {

    AddressMapper addressMapper;

    @Test
    void toEntityTest() {
        PnServiceDeskConfigs pnServiceDeskConfigs= new PnServiceDeskConfigs();
        pnServiceDeskConfigs.setTtlReceiverAddress(Long.valueOf("1"));
        Instant instant = LocalDateTime.now().plusDays(pnServiceDeskConfigs.getTtlReceiverAddress()).toInstant(ZoneOffset.UTC);
        AnalogAddress analogAddress= getAnalogAddress();
        PnServiceDeskAddress pnServiceDeskAddress= addressMapper.toEntity(analogAddress, "1234",  pnServiceDeskConfigs);
        assertEquals(pnServiceDeskAddress.getFullName(), analogAddress.getFullname());
        assertEquals(pnServiceDeskAddress.getAddress(), analogAddress.getAddress());
        assertEquals(pnServiceDeskAddress.getAddressRow2(), analogAddress.getAddressRow2());
        assertEquals(pnServiceDeskAddress.getCap(), analogAddress.getCap());
        assertEquals(pnServiceDeskAddress.getCity(), analogAddress.getCity());
        assertEquals(pnServiceDeskAddress.getCity2(), analogAddress.getCity2());
        assertEquals(pnServiceDeskAddress.getPr(), analogAddress.getPr());
        assertEquals(pnServiceDeskAddress.getCountry(), analogAddress.getCountry());
        assertEquals(pnServiceDeskAddress.getTtl(), instant.getEpochSecond());

    }

    @Test
    void exceptionConstructorTest() throws  NoSuchMethodException {
        Constructor<AddressMapper> constructor = AddressMapper.class.getDeclaredConstructor();
        Assertions.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        Exception exception = Assertions.assertThrows(Exception.class, () -> constructor.newInstance());
        Assertions.assertEquals(null, exception.getMessage());
    }

    @Test
    void toAnalogAddressManager() {
        AnalogAddressDto analogAddressDto= addressMapper.toAnalogAddressManager(getPnServiceDeskAddress());
        assertNotNull(analogAddressDto);
    }

    @Test
    void toAnalogAddressDto() {
        it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.AnalogAddressDto analogAddressDto= addressMapper.toAnalogAddressDto(getSenderAddress());
        assertNotNull(analogAddressDto);
    }

    @Test
    void toPreparePaperAddress() {
        it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.AnalogAddressDto analogAddressDto= addressMapper.toPreparePaperAddress(getPnServiceDeskAddress());
        assertNotNull(analogAddressDto);
    }


    PnServiceDeskAddress getPnServiceDeskAddress() {
        PnServiceDeskAddress pnServiceDeskAddress= new PnServiceDeskAddress();
        pnServiceDeskAddress.setAddress("Via Roma");
        pnServiceDeskAddress.setAddressRow2("Via Napoli");
        pnServiceDeskAddress.setCap("82100");
        pnServiceDeskAddress.setCity("Napoli");
        pnServiceDeskAddress.setCity2("Napoli");
        pnServiceDeskAddress.setPr("NA");
        pnServiceDeskAddress.setCountry("Italia");
        return pnServiceDeskAddress;
    }

    AnalogAddress getAnalogAddress() {
        AnalogAddress analogAddress= new AnalogAddress();
        analogAddress.setFullname("Mario Rossi");
        analogAddress.setAddress("Via Roma");
        analogAddress.setAddressRow2("Via Napoli");
        analogAddress.setCap("10800");
        analogAddress.setCity("Napoli");
        analogAddress.setCity2("Roma");
        analogAddress.setPr("NA");
        analogAddress.setCountry("Italia");
        return analogAddress;
    }

    PnServiceDeskConfigs.SenderAddress getSenderAddress() {
        PnServiceDeskConfigs.SenderAddress senderAddress= new PnServiceDeskConfigs.SenderAddress();
        senderAddress.setFullname("Mario Rossi");
        senderAddress.setAddress("Via Roma");
        senderAddress.setCity("Napoli");
        senderAddress.setPr("NA");
        senderAddress.setCountry("Italia");
        return senderAddress;
    }



}