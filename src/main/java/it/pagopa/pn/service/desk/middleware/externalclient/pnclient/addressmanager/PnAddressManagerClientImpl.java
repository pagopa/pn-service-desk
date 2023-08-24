package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.addressmanager;

import it.pagopa.pn.paperchannel.generated.openapi.msclient.pnaddressmanager.v1.api.DeduplicatesAddressServiceApi;
import it.pagopa.pn.paperchannel.generated.openapi.msclient.pnaddressmanager.v1.dto.DeduplicatesRequestDto;
import it.pagopa.pn.paperchannel.generated.openapi.msclient.pnaddressmanager.v1.dto.DeduplicatesResponseDto;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.mapper.AddressMapper;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.ConnectException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class PnAddressManagerClientImpl implements PnAddressManagerClient{

    private final PnServiceDeskConfigs cnf;

    @Autowired
    private DeduplicatesAddressServiceApi serviceApi;

    public PnAddressManagerClientImpl(PnServiceDeskConfigs cnf, DeduplicatesAddressServiceApi serviceApi) {
        this.cnf = cnf;
        this.serviceApi = serviceApi;
    }


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
                )
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(500))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException)
                );
    }
}
