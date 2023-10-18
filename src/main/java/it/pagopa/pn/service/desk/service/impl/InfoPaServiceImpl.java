package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.v1.dto.PaSummaryDto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.PaSummary;
import it.pagopa.pn.service.desk.mapper.common.BaseMapper;
import it.pagopa.pn.service.desk.mapper.common.BaseMapperImpl;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.externalregistries.ExternalRegistriesClient;
import it.pagopa.pn.service.desk.service.InfoPaService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class InfoPaServiceImpl implements InfoPaService {

    private final ExternalRegistriesClient externalRegistriesClient;

    private static final BaseMapper<PaSummary, PaSummaryDto> baseMapper = new BaseMapperImpl<>(PaSummary.class, PaSummaryDto.class);

    public InfoPaServiceImpl(ExternalRegistriesClient externalRegistriesClient) {
        this.externalRegistriesClient = externalRegistriesClient;
    }

    @Override
    public Flux<PaSummary> getListOfOnboardedPA(String xPagopaPnUid) {
        return externalRegistriesClient.listOnboardedPa()
                .map(baseMapper::toEntity);
    }
}
