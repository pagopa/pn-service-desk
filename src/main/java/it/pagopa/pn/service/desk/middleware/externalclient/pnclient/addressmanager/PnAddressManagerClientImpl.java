package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.addressmanager;

import it.pagopa.pn.paperchannel.generated.openapi.msclient.pnaddressmanager.v1.api.DeduplicatesAddressServiceApi;
import it.pagopa.pn.paperchannel.generated.openapi.msclient.pnaddressmanager.v1.dto.DeduplicatesRequestDto;
import it.pagopa.pn.paperchannel.generated.openapi.msclient.pnaddressmanager.v1.dto.DeduplicatesResponseDto;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.mapper.AddressMapper;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Slf4j
@Component
@AllArgsConstructor
public class PnAddressManagerClientImpl implements PnAddressManagerClient{
    private PnServiceDeskConfigs cnf;
    private DeduplicatesAddressServiceApi serviceApi;


    @Override
    public Mono<DeduplicatesResponseDto> deduplicates(PnServiceDeskAddress address) {

        DeduplicatesRequestDto requestDto = new DeduplicatesRequestDto();

        requestDto.setBaseAddress(AddressMapper.toAnalogAddressManager(address));
        requestDto.setTargetAddress(AddressMapper.toAnalogAddressManager(address));
        requestDto.setCorrelationId(UUID.randomUUID().toString());

        return this.serviceApi.deduplicates(
                        this.cnf.getAddressManagerCxId(),
                        this.cnf.getAddressManagerApiKey(),
                        requestDto
                );
    }
}
