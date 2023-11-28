package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.v1.dto.PaSummaryDto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.PaNotificationsRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.PaSummary;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.SearchNotificationsResponse;
import it.pagopa.pn.service.desk.mapper.common.BaseMapper;
import it.pagopa.pn.service.desk.mapper.common.BaseMapperImpl;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.externalregistries.ExternalRegistriesClient;
import it.pagopa.pn.service.desk.service.AuditLogService;
import it.pagopa.pn.service.desk.service.InfoPaService;
import it.pagopa.pn.service.desk.mapper.InfoPaMapper;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_ON_DELIVERY_CLIENT;

@Service
@CustomLog
public class InfoPaServiceImpl implements InfoPaService {

    private final ExternalRegistriesClient externalRegistriesClient;
    private final PnDeliveryClient pnDeliveryClient;
    private static final BaseMapper<PaSummary, PaSummaryDto> baseMapper = new BaseMapperImpl<>(PaSummary.class, PaSummaryDto.class);
    @Autowired
    private final AuditLogService auditLogService;

    public InfoPaServiceImpl(ExternalRegistriesClient externalRegistriesClient, PnDeliveryClient pnDeliveryClient, AuditLogService auditLogService) {
        this.externalRegistriesClient = externalRegistriesClient;
        this.pnDeliveryClient = pnDeliveryClient;
        this.auditLogService = auditLogService;
    }

    @Override
    public Flux<PaSummary> getListOfOnboardedPA(String xPagopaPnUid) {
        return externalRegistriesClient.listOnboardedPa()
                .map(baseMapper::toEntity);
    }

    @Override
    public Mono<SearchNotificationsResponse> searchNotificationsFromSenderId(String xPagopaPnUid, Integer size, String nextPagesKey, PaNotificationsRequest paNotificationsRequest) {
        PnAuditLogEvent logEvent =  auditLogService.buildAuditLogEvent(PnAuditLogEventType.AUD_NT_INSERT, "searchNotificationsFromSenderId for senderId = {}", paNotificationsRequest.getId());
        return this.pnDeliveryClient.searchNotificationsPrivate(paNotificationsRequest.getStartDate(), paNotificationsRequest.getEndDate(), null, paNotificationsRequest.getId(), size, nextPagesKey)
                .onErrorResume(exception -> {
                    log.error("errorReason = {}, An error occurred while calling the service to obtain sent notifications", exception.getMessage());
                    logEvent.generateFailure("errorReason = {}, An error occurred while calling the service to obtain sent notifications", exception.getMessage()).log();
                    return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_CLIENT, exception.getMessage()));
                })
                .map(notificationSearchResponseDto -> {
                    SearchNotificationsResponse response = InfoPaMapper.getSearchNotificationResponse(notificationSearchResponseDto);
                    logEvent.generateSuccess("searchNotificationsFromSenderId response = {}", response).log();
                    return response;
                });
    }


}
