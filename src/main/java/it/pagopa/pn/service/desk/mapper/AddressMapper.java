package it.pagopa.pn.service.desk.mapper;


import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnaddressmanager.v1.dto.AnalogAddressDto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.AnalogAddress;
import it.pagopa.pn.service.desk.mapper.common.BaseMapper;
import it.pagopa.pn.service.desk.mapper.common.BaseMapperImpl;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class AddressMapper {
    private static final BaseMapper<PnServiceDeskAddress, AnalogAddress> mapper = new BaseMapperImpl<>(PnServiceDeskAddress.class, AnalogAddress.class);

    private AddressMapper(){
        throw new IllegalCallerException("the constructor must not called");
    }

    public static PnServiceDeskAddress toEntity(AnalogAddress address, String operationId, PnServiceDeskConfigs configs){
        PnServiceDeskAddress pnAddress = mapper.toEntity(address);
        pnAddress.setOperationId(operationId);
        if (configs != null && configs.getTtlReceiverAddress() != null) {
            Instant instant = LocalDateTime.now().plusDays(configs.getTtlReceiverAddress()).toInstant(ZoneOffset.UTC);
            pnAddress.setTtl(instant.getEpochSecond());
        }

        return pnAddress;
    }

    public static AnalogAddressDto toAnalogAddressManager(PnServiceDeskAddress address){
        AnalogAddressDto analogAddress = new AnalogAddressDto();
        analogAddress.setAddressRow(address.getAddress());
        analogAddress.setAddressRow2(address.getAddressRow2());
        analogAddress.setCap(address.getCap());
        analogAddress.setCity(address.getCity());
        analogAddress.setCity2(address.getCity2());
        analogAddress.setPr(address.getPr());
        analogAddress.setCountry(address.getCountry());
        return analogAddress;
    }

}
