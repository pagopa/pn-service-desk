package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.TimelineElementCategoryDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.TimelineElementDto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.SearchNotificationsRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.SearchNotificationsResponse;
import it.pagopa.pn.service.desk.mapper.NotificationAndMessageMapper;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault.PnDataVaultClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClient;
import it.pagopa.pn.service.desk.service.NotificationAndMessageService;
import lombok.CustomLog;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_ON_DELIVERY_CLIENT;
import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_ON_DELIVERY_PUSH_CLIENT;

@Service
@CustomLog
public class NotificationAndMessageServiceImpl implements NotificationAndMessageService {

    private final PnDataVaultClient dataVaultClient;
    private final PnDeliveryClient pnDeliveryClient;
    private final PnDeliveryPushClient pnDeliveryPushClient;

    public NotificationAndMessageServiceImpl(PnDataVaultClient dataVaultClient, PnDeliveryClient pnDeliveryClient, PnDeliveryPushClient pnDeliveryPushClient) {
        this.dataVaultClient = dataVaultClient;
        this.pnDeliveryClient = pnDeliveryClient;
        this.pnDeliveryPushClient = pnDeliveryPushClient;
    }

    @Override
    public Mono<SearchNotificationsResponse> searchNotificationsFromTaxId(String xPagopaPnUid, OffsetDateTime startDate, OffsetDateTime endDate, Integer size, String nextPagesKey, SearchNotificationsRequest request) {
        SearchNotificationsResponse response = new SearchNotificationsResponse();
        return dataVaultClient.anonymized(request.getTaxId(), request.getRecipientType().getValue())
                .flatMap(taxId ->
                        pnDeliveryClient.searchNotificationsPrivate(startDate, endDate, taxId, null, size, nextPagesKey)
                                .onErrorResume(exception -> {
                                    log.error("errorReason = {}, An error occurred while calling the service to obtain sent notifications", exception.getMessage());
                                    return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_CLIENT, exception.getMessage()));
                                })
                )
                .switchIfEmpty(Mono.empty())
                .flatMapMany(notificationSearchResponseDto -> {
                    response.setNextPagesKey(notificationSearchResponseDto.getNextPagesKey());
                    response.setMoreResult(notificationSearchResponseDto.getMoreResult());
                    if (notificationSearchResponseDto.getResultsPage() != null) {
                        return Flux.fromIterable(notificationSearchResponseDto.getResultsPage());
                    }
                    return Flux.empty();
                })
                .flatMap(notificationSearchRowDto ->
                        this.pnDeliveryPushClient.getNotificationHistory(notificationSearchRowDto.getIun(), notificationSearchRowDto.getRecipients().size(), notificationSearchRowDto.getSentAt())
                                .switchIfEmpty(Mono.defer(() -> {
                                    NotificationAndMessageMapper.getNotification(notificationSearchRowDto, null);
                                    return Mono.empty();
                                }))
                                .onErrorResume(exception -> {
                                    log.error("errorReason = {}, An error occurred while calling the service to obtain sent notifications", exception.getMessage());
                                    return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_PUSH_CLIENT, exception.getMessage()));
                                })
                                .flatMap(notificationHistoryResponseDto -> {
                                    List<TimelineElementDto> filteredElements = notificationHistoryResponseDto.getTimeline()
                                            .stream()
                                            .filter(element -> element.getCategory().equals(TimelineElementCategoryDto.SEND_COURTESY_MESSAGE))
                                            .collect(Collectors.toList());
                                    return Mono.just(NotificationAndMessageMapper.getNotification(notificationSearchRowDto, filteredElements));
                                })
                )
                .collectList()
                .map(notifications -> {
                    response.setResults(notifications);
                    return response;
                });
    }
}
