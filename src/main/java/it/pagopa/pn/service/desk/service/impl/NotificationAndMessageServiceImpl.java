package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.TimelineElementCategoryV20Dto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.TimelineElementV20Dto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
                .flatMap(internalId ->
                        pnDeliveryClient.searchNotificationsPrivate(startDate, endDate, internalId, null, size, nextPagesKey)
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
                                .onErrorResume(exception -> {
                                    log.error("errorReason = {}, An error occurred while calling the service to obtain sent notifications", exception.getMessage());
                                    return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_PUSH_CLIENT, exception.getMessage()));
                                })
                                .map(notificationHistoryResponseDto -> {
                                    List<TimelineElementV20Dto> filteredElements = new ArrayList<>();
                                    if (notificationHistoryResponseDto.getTimeline() != null) {
                                        filteredElements = notificationHistoryResponseDto.getTimeline()
                                                .stream()
                                                .filter(element -> element.getCategory().equals(TimelineElementCategoryV20Dto.SEND_COURTESY_MESSAGE))
                                                .collect(Collectors.toList());
                                    }
                                    return NotificationAndMessageMapper.getNotification(notificationSearchRowDto, filteredElements);
                                })
                )
                .collectList()
                .map(notifications -> {
                    response.setResults(notifications);
                    return response;
                });
    }

    @Override
    public Mono<TimelineResponse> getTimelineOfIUN(String xPagopaPnUid, String iun) {
        return pnDeliveryClient.getSentNotificationPrivate(iun)
                .onErrorResume(exception -> {
                    log.error("errorReason = {}, An error occurred while call service for obtain notification sent", exception.getMessage());
                    return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_CLIENT, exception.getMessage()));
                })
                .flatMap(sentNotificationDto ->
                        pnDeliveryPushClient.getNotificationHistory(iun, sentNotificationDto.getRecipients().size(), sentNotificationDto.getSentAt())
                                .switchIfEmpty(Mono.empty())
                                .onErrorResume(exception -> {
                                    log.error("errorReason = {}, An error occurred while call service for obtain notification history", exception.getMessage());
                                    return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_CLIENT, exception.getMessage()));
                                })
                                .map(NotificationAndMessageMapper::getTimeline)
                );
    }

    @Override
    public Mono<DocumentsResponse> getDocumentsOfIun(String iun, DocumentsRequest request) {
        DocumentsResponse response = new DocumentsResponse();
        AtomicInteger documentsSize = new AtomicInteger(0);
        return dataVaultClient.anonymized(request.getTaxId(), request.getRecipientType().getValue())
                .zipWhen(internalId -> pnDeliveryClient.getSentNotificationPrivate(iun)
                        .switchIfEmpty(Mono.empty())
                        .onErrorResume(exception -> {
                            log.error("errorReason = {}, An error occurred while calling the service to obtain sent notifications", exception.getMessage());
                            return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_CLIENT, exception.getMessage()));
                        })
                )
                .flatMapMany(internalIdAndSentNotificationDto -> {
                    if (!isNotificationCancelled(internalIdAndSentNotificationDto.getT2(), iun)) {
                        response.setDocumentsAvailable(true);
                        return Flux.fromIterable(internalIdAndSentNotificationDto.getT2().getDocuments())
                                .flatMap(notificationDocumentDto ->
                                        pnDeliveryClient.getReceivedNotificationDocumentPrivate(iun, Integer.parseInt(notificationDocumentDto.getDocIdx()), internalIdAndSentNotificationDto.getT1(), null)
                                                .switchIfEmpty(Mono.empty())
                                                .onErrorResume(exception -> {
                                                    log.error("errorReason = {}, An error occurred while calling the service to obtain sent notifications", exception.getMessage());
                                                    return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_CLIENT, exception.getMessage()));
                                                }))
                                .map(notificationAttachmentDownloadMetadataResponseDto -> {
                                    documentsSize.set(documentsSize.get() + notificationAttachmentDownloadMetadataResponseDto.getContentLength());
                                    return NotificationAndMessageMapper.getDocument(notificationAttachmentDownloadMetadataResponseDto);
                                });
                    }
                    return Flux.empty();
                })
                .switchIfEmpty(Mono.empty())
                .collectList()
                .map(documentList -> {
                    response.setDocuments(documentList);
                    response.setTotalSize(documentsSize.get());
                    return response;
                });
    }

    private boolean isNotificationCancelled(SentNotificationDto sentNotificationDto, String iun) {
        AtomicBoolean cancellationTimelineIsPresent = new AtomicBoolean();
        return pnDeliveryPushClient.getNotificationHistory(iun, sentNotificationDto.getRecipients().size(), sentNotificationDto.getSentAt())
                .switchIfEmpty(Mono.empty())
                .onErrorResume(exception -> {
                    log.error("errorReason = {}, An error occurred while call service for obtain notification history", exception.getMessage());
                    return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_CLIENT, exception.getMessage()));
                }).map(notificationHistoryResponseDto -> {
                    var cancellationRequestCategory = TimelineElementCategoryV20Dto.NOTIFICATION_CANCELLATION_REQUEST;
                    Optional<TimelineElementV20Dto> cancellationRequestTimeline = notificationHistoryResponseDto.getTimeline().stream()
                            .filter(timelineElement -> cancellationRequestCategory.toString().equals(timelineElement.getCategory().toString()))
                            .findFirst();
                    cancellationTimelineIsPresent.set(cancellationRequestTimeline.isPresent());
                    if (cancellationTimelineIsPresent.get()) {
                        log.warn("Notification with iun: {} has a request for cancellation", iun);
                    }
                    return cancellationTimelineIsPresent.get();
                })
                .flatMap(Mono::justOrEmpty)
                .defaultIfEmpty(false)
                .subscribe(cancellationTimelineIsPresent::set)
                .isDisposed();
    }

}
