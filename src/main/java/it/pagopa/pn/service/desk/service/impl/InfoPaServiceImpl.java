package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.v1.dto.PaSummaryDto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.PaNotificationsRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.PaSummary;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.SearchNotificationsResponse;
import it.pagopa.pn.service.desk.mapper.common.BaseMapper;
import it.pagopa.pn.service.desk.mapper.common.BaseMapperImpl;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.externalregistries.ExternalRegistriesClient;
import it.pagopa.pn.service.desk.service.InfoPaService;
import it.pagopa.pn.service.desk.mapper.InfoPaMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class InfoPaServiceImpl implements InfoPaService {

    private final ExternalRegistriesClient externalRegistriesClient;
    private final PnDeliveryClient pnDeliveryClient;

    private static final BaseMapper<PaSummary, PaSummaryDto> baseMapper = new BaseMapperImpl<>(PaSummary.class, PaSummaryDto.class);

    public InfoPaServiceImpl(ExternalRegistriesClient externalRegistriesClient, PnDeliveryClient pnDeliveryClient) {
        this.externalRegistriesClient = externalRegistriesClient;
        this.pnDeliveryClient = pnDeliveryClient;
    }

    @Override
    public Flux<PaSummary> getListOfOnboardedPA(String xPagopaPnUid) {
        return externalRegistriesClient.listOnboardedPa()
                .map(baseMapper::toEntity);
    }

    @Override
    public Mono<SearchNotificationsResponse> searchNotificationsFromSenderId(String xPagopaPnUid, Integer size, String nextPagesKey, PaNotificationsRequest paNotificationsRequest) {
        return this.pnDeliveryClient.searchNotificationsPrivate(paNotificationsRequest.getStartDate(), paNotificationsRequest.getEndDate(), null, paNotificationsRequest.getId(), size, nextPagesKey)
                .map(InfoPaMapper::getSearchNotificationResponse);
    }


}
