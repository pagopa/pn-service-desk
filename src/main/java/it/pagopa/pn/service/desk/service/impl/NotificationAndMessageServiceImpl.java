package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationSearchResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationSearchRowDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationV21Dto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.NotificationHistoryResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.TimelineElementCategoryV20Dto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.TimelineElementV20Dto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.service.desk.mapper.NotificationAndMessageMapper;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault.PnDataVaultClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClient;
import it.pagopa.pn.service.desk.service.NotificationAndMessageService;
import lombok.CustomLog;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_ON_DELIVERY_CLIENT;
import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_ON_DELIVERY_PUSH_CLIENT;

@Service
@CustomLog
public class NotificationAndMessageServiceImpl implements NotificationAndMessageService {

    private final PnDataVaultClient dataVaultClient;
    private final PnDeliveryClient pnDeliveryClient;
    private final PnDeliveryPushClient pnDeliveryPushClient;
    private final PnAuditLogBuilder auditLogBuilder = new PnAuditLogBuilder();
    private static final String ERROR_MESSAGE_NOTIFICATION_HISTORY = "errorReason = {}, An error occurred while call service for obtain notification history";
    private static final String ERROR_MESSAGE_SENT_NOTIFICATIONS = "errorReason = {}, An error occurred while calling the service to obtain sent notifications";
    private static final String ERROR_MESSAGE_SEARCH_NOTIFICATIONS = "errorReason = {}, An error occurred while calling the service to obtain sent notifications";

    public NotificationAndMessageServiceImpl(PnDataVaultClient dataVaultClient, PnDeliveryClient pnDeliveryClient, PnDeliveryPushClient pnDeliveryPushClient) {
        this.dataVaultClient = dataVaultClient;
        this.pnDeliveryClient = pnDeliveryClient;
        this.pnDeliveryPushClient = pnDeliveryPushClient;
    }

    @Override
    public Mono<SearchNotificationsResponse> searchNotificationsFromTaxId(String xPagopaPnUid, OffsetDateTime startDate, OffsetDateTime endDate, Integer size, String nextPagesKey, SearchNotificationsRequest request) {
        SearchNotificationsResponse response = new SearchNotificationsResponse();
        PnAuditLogEvent logEvent = auditLogBuilder.before(PnAuditLogEventType.AUD_NT_INSERT, "searchNotificationsFromTaxId for taxId = {}", request.getTaxId())
                .build().log();
        return dataVaultClient.anonymized(request.getTaxId(), request.getRecipientType().getValue())
                .flatMap(internalId ->
                        pnDeliveryClient.searchNotificationsPrivate(startDate, endDate, internalId, null, size, nextPagesKey)
                                .onErrorResume(exception -> {
                                    log.error(ERROR_MESSAGE_SEARCH_NOTIFICATIONS, exception.getMessage());
                                    logEvent.generateFailure(ERROR_MESSAGE_SEARCH_NOTIFICATIONS, exception.getMessage()).log();
                                    return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_CLIENT, exception.getMessage()));
                                })
                )
                .switchIfEmpty(Mono.empty())
                .flatMapMany(notificationSearchResponseDto -> getNotificationSearchRowFlux(notificationSearchResponseDto, response))
                .flatMap(notificationSearchRowDto ->
                        this.pnDeliveryPushClient.getNotificationHistory(notificationSearchRowDto.getIun(), notificationSearchRowDto.getRecipients().size(), notificationSearchRowDto.getSentAt())
                                .onErrorResume(exception -> {
                                    log.error(ERROR_MESSAGE_NOTIFICATION_HISTORY, exception.getMessage());
                                    logEvent.generateFailure(ERROR_MESSAGE_NOTIFICATION_HISTORY, exception.getMessage()).log();
                                    return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_PUSH_CLIENT, exception.getMessage()));
                                })
                                .map(notificationHistoryResponseDto -> NotificationAndMessageMapper
                                        .getNotification(notificationSearchRowDto, getFilteredElements(notificationHistoryResponseDto)))
                )
                .collectList()
                .map(notifications -> {
                    response.setResults(notifications);
                    logEvent.generateSuccess("searchNotificationsFromTaxId response = {}", response).log();
                    return response;
                });
    }

    @NotNull
    private static Flux<NotificationSearchRowDto> getNotificationSearchRowFlux(NotificationSearchResponseDto notificationSearchResponseDto, SearchNotificationsResponse response) {
        response.setNextPagesKey(notificationSearchResponseDto.getNextPagesKey());
        response.setMoreResult(notificationSearchResponseDto.getMoreResult());
        if (notificationSearchResponseDto.getResultsPage() != null) {
            return Flux.fromIterable(notificationSearchResponseDto.getResultsPage());
        }
        return Flux.empty();
    }

    @NotNull
    private static List<TimelineElementV20Dto> getFilteredElements(NotificationHistoryResponseDto notificationHistoryResponseDto) {
        List<TimelineElementV20Dto> filteredElements = new ArrayList<>();
        if (notificationHistoryResponseDto.getTimeline() != null) {
            filteredElements = notificationHistoryResponseDto.getTimeline()
                    .stream()
                    .filter(element -> element.getCategory().equals(TimelineElementCategoryV20Dto.SEND_COURTESY_MESSAGE))
                    .toList();
        }
        return filteredElements;
    }

    @Override
    public Mono<TimelineResponse> getTimelineOfIUN(String xPagopaPnUid, String iun) {
        PnAuditLogEvent logEvent = auditLogBuilder.before(PnAuditLogEventType.AUD_NT_INSERT, "getTimelineOfIUN for IUN = {}", iun)
                .build().log();
        return pnDeliveryClient.getSentNotificationPrivate(iun)
                .onErrorResume(exception -> {
                    log.error(ERROR_MESSAGE_SENT_NOTIFICATIONS, exception.getMessage());
                    logEvent.generateFailure(ERROR_MESSAGE_SENT_NOTIFICATIONS, exception.getMessage()).log();
                    return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_CLIENT, exception.getMessage()));
                })
                .flatMap(sentNotificationV21Dto ->
                        pnDeliveryPushClient.getNotificationHistory(iun, sentNotificationV21Dto.getRecipients().size(), sentNotificationV21Dto.getSentAt())
                                .switchIfEmpty(Mono.empty())
                                .onErrorResume(exception -> {
                                    log.error(ERROR_MESSAGE_NOTIFICATION_HISTORY, exception.getMessage());
                                    logEvent.generateFailure(ERROR_MESSAGE_NOTIFICATION_HISTORY, exception.getMessage()).log();
                                    return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_PUSH_CLIENT, exception.getMessage()));
                                })
                                .map(historyResponseDto -> {
                                    TimelineResponse response = NotificationAndMessageMapper.getTimeline(historyResponseDto);
                                    logEvent.generateSuccess("getTimelineOfIUN response = {}", response).log();
                                    return response;
                                })
                );
    }

    @Override
    public Mono<DocumentsResponse> getDocumentsOfIun(String iun, DocumentsRequest request) {
        PnAuditLogEvent logEvent = auditLogBuilder.before(PnAuditLogEventType.AUD_NT_INSERT, "getDocumentsOfIun for IUN = {}", iun)
                .build().log();
        DocumentsResponse response = new DocumentsResponse();
        AtomicInteger documentsSize = new AtomicInteger(0);
        return dataVaultClient.anonymized(request.getTaxId(), request.getRecipientType().getValue())
                .zipWhen(internalId -> pnDeliveryClient.getSentNotificationPrivate(iun)
                        .switchIfEmpty(Mono.empty())
                        .onErrorResume(exception -> {
                            log.error(ERROR_MESSAGE_SENT_NOTIFICATIONS, exception.getMessage());
                            logEvent.generateFailure(ERROR_MESSAGE_SENT_NOTIFICATIONS, exception.getMessage()).log();
                            return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_CLIENT, exception.getMessage()));
                        })
                )
                .flatMapMany(internalIdAndSentNotificationV21Dto ->
                        getDocuments(iun, internalIdAndSentNotificationV21Dto, response, documentsSize)
                )
                .switchIfEmpty(Mono.empty())
                .collectList()
                .map(documentList -> {
                    response.setDocuments(documentList);
                    response.setTotalSize(documentsSize.get());
                    logEvent.generateSuccess("getDocumentsOfIun response = {}", response);
                    return response;
                });
    }

    @NotNull
    private Flux<Document> getDocuments(String iun, Tuple2<String, SentNotificationV21Dto> internalIdAndSentNotificationV21Dto, DocumentsResponse response, AtomicInteger documentsSize) {
        PnAuditLogEvent logEvent = auditLogBuilder.before(PnAuditLogEventType.AUD_NT_INSERT, "mapping getDocuments")
                .build().log();
        if (!isNotificationCancelled(internalIdAndSentNotificationV21Dto.getT2(), iun)) {
            logEvent.generateSuccess("isNotificationCancelled = false");
            response.setDocumentsAvailable(true);
            return Flux.fromIterable(internalIdAndSentNotificationV21Dto.getT2().getDocuments())
                    .flatMap(notificationDocumentDto ->
                            pnDeliveryClient.getReceivedNotificationDocumentPrivate(iun, Integer.parseInt(notificationDocumentDto.getDocIdx()), internalIdAndSentNotificationV21Dto.getT1(), null)
                                    .switchIfEmpty(Mono.empty())
                                    .onErrorResume(exception -> {
                                        log.error("errorReason = {}, An error occurred while calling the service to obtain notification document", exception.getMessage());
                                        logEvent.generateFailure("errorReason = {}, An error occurred while calling the service to obtain notification document", exception.getMessage());
                                        return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_CLIENT, exception.getMessage()));
                                    }))
                    .map(notificationAttachmentDownloadMetadataResponseDto -> {
                        documentsSize.set(documentsSize.get() + notificationAttachmentDownloadMetadataResponseDto.getContentLength());
                        Document document = NotificationAndMessageMapper.getDocument(notificationAttachmentDownloadMetadataResponseDto);
                        logEvent.generateSuccess("mapping Document = {}", document);
                        return document;
                    });
        }
        logEvent.generateSuccess("isNotificationCancelled = true");
        return Flux.empty();
    }

    @Override
    public Mono<NotificationDetailResponse> getNotificationFromIUN(String iun) {
        PnAuditLogEvent logEvent = auditLogBuilder.before(PnAuditLogEventType.AUD_NT_INSERT, "getNotificationFromIUN for IUN = {}", iun)
                .build().log();
        return this.pnDeliveryClient.getSentNotificationPrivate(iun)
                .switchIfEmpty(Mono.empty())
                .onErrorResume(exception -> {
                    log.error(ERROR_MESSAGE_SENT_NOTIFICATIONS, exception.getMessage());
                    logEvent.generateFailure(ERROR_MESSAGE_SENT_NOTIFICATIONS, exception.getMessage());
                    return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_CLIENT, exception.getMessage()));
                })
                .map(sentNotificationV21Dto -> {
                    NotificationDetailResponse response = NotificationAndMessageMapper.getNotificationDetail(sentNotificationV21Dto);
                    logEvent.generateSuccess("getNotificationFromIUN response = {}", response);
                    return response;
                });
    }

    @Override
    public Mono<SearchNotificationsResponse> searchNotificationsAsDelegateFromInternalId(String xPagopaPnUid, String mandateId, String delegateInternalId, Integer size, String nextPagesKey, OffsetDateTime startDate, OffsetDateTime endDate) {
        PnAuditLogEvent logEvent = auditLogBuilder.before(PnAuditLogEventType.AUD_NT_INSERT, "searchNotificationsAsDelegateFromInternalId for delegateInternalId = {}", delegateInternalId)
                .build().log();
        SearchNotificationsResponse searchNotificationsResponse = new SearchNotificationsResponse();
        return pnDeliveryClient.searchNotificationsPrivate(startDate, endDate, delegateInternalId, mandateId, size, nextPagesKey)
                .onErrorResume(exception -> {
                    log.error(ERROR_MESSAGE_SEARCH_NOTIFICATIONS, exception.getMessage());
                    logEvent.generateFailure(ERROR_MESSAGE_SEARCH_NOTIFICATIONS, exception.getMessage());
                    return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_CLIENT, exception.getMessage()));
                })
                .switchIfEmpty(Mono.empty())
                .flatMapMany(notificationSearchResponseDto -> getNotificationSearchRowFlux(notificationSearchResponseDto, searchNotificationsResponse))
                .flatMap(notificationSearchRowDto -> pnDeliveryPushClient.getNotificationHistory(notificationSearchRowDto.getIun(), notificationSearchRowDto.getRecipients().size(), notificationSearchRowDto.getSentAt())
                        .onErrorResume(exception -> {
                            log.error(ERROR_MESSAGE_NOTIFICATION_HISTORY, exception.getMessage());
                            logEvent.generateFailure(ERROR_MESSAGE_NOTIFICATION_HISTORY, exception.getMessage());
                            return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_PUSH_CLIENT, exception.getMessage()));
                        })
                        .map(notificationHistoryResponseDto -> NotificationAndMessageMapper
                                .getNotificationResponse(notificationSearchRowDto, notificationHistoryResponseDto)))
                .collectList()
                .map(notificationResponses -> {
                    searchNotificationsResponse.setResults(notificationResponses);
                    logEvent.generateSuccess("searchNotificationsAsDelegateFromInternalId response = {}", searchNotificationsResponse);
                    return searchNotificationsResponse;
                });
    }

    private boolean isNotificationCancelled(SentNotificationV21Dto sentNotificationV21Dto, String iun) {
        PnAuditLogEvent logEvent = auditLogBuilder.before(PnAuditLogEventType.AUD_NT_INSERT, "isNotificationCancelled")
                .build().log();
        AtomicBoolean cancellationTimelineIsPresent = new AtomicBoolean();
        return pnDeliveryPushClient.getNotificationHistory(iun, sentNotificationV21Dto.getRecipients().size(), sentNotificationV21Dto.getSentAt())
                .switchIfEmpty(Mono.empty())
                .onErrorResume(exception -> {
                    log.error(ERROR_MESSAGE_NOTIFICATION_HISTORY, exception.getMessage());
                    logEvent.generateFailure(ERROR_MESSAGE_NOTIFICATION_HISTORY, exception.getMessage());
                    return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_PUSH_CLIENT, exception.getMessage()));
                }).map(notificationHistoryResponseDto ->
                        cancellationTimelineIsPresent(iun, notificationHistoryResponseDto, cancellationTimelineIsPresent)
                )
                .flatMap(Mono::justOrEmpty)
                .defaultIfEmpty(false)
                .subscribe(cancellationTimelineIsPresent::set)
                .isDisposed();
    }

    private static boolean cancellationTimelineIsPresent(String iun, NotificationHistoryResponseDto notificationHistoryResponseDto, AtomicBoolean cancellationTimelineIsPresent) {
        var cancellationRequestCategory = TimelineElementCategoryV20Dto.NOTIFICATION_CANCELLATION_REQUEST;
        Optional<TimelineElementV20Dto> cancellationRequestTimeline = notificationHistoryResponseDto.getTimeline().stream()
                .filter(timelineElement -> cancellationRequestCategory.toString().equals(timelineElement.getCategory().toString()))
                .findFirst();
        cancellationTimelineIsPresent.set(cancellationRequestTimeline.isPresent());
        if (cancellationTimelineIsPresent.get()) {
            log.warn("Notification with iun: {} has a request for cancellation", iun);
        }
        return cancellationTimelineIsPresent.get();
    }

}
