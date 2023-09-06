package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnaddressmanager.v1.dto.AnalogAddressDto;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class AddressMapperTest {

    AddressMapper addressMapper;

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