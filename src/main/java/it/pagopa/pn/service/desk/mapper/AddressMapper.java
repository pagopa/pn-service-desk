package it.pagopa.pn.service.desk.mapper;


import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.AnalogAddress;
import it.pagopa.pn.service.desk.mapper.common.BaseMapper;
import it.pagopa.pn.service.desk.mapper.common.BaseMapperImpl;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class AddressMapper {
    private static final BaseMapper<PnServiceDeskAddress, AnalogAddress> mapper = new BaseMapperImpl<>(PnServiceDeskAddress.class, AnalogAddress.class);

    private AddressMapper(){
        throw new IllegalCallerException("the constructor must not called");
    }

    public static PnServiceDeskAddress toEntity(AnalogAddress address, String operationId){

        PnServiceDeskAddress pnAddress = mapper.toEntity(address);
        pnAddress.setOperationId(operationId);
        Instant instant = LocalDateTime.now().plusDays(120).toInstant(ZoneOffset.UTC);
        pnAddress.setTtl(instant.toEpochMilli());

        return pnAddress;
    }

}
