package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationRecipientV23Dto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationSearchResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationSearchRowDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationV23Dto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.NotificationHistoryResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.TimelineElementCategoryV20Dto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.TimelineElementV20Dto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.service.desk.mapper.NotificationAndMessageMapper;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault.PnDataVaultClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClient;
import it.pagopa.pn.service.desk.service.AuditLogService;
import it.pagopa.pn.service.desk.service.NotificationAndMessageService;
import lombok.CustomLog;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.*;

@Service
@CustomLog
public class NotificationAndMessageServiceImpl implements NotificationAndMessageService {

    private final PnDataVaultClient dataVaultClient;
    private final PnDeliveryClient pnDeliveryClient;
    private final PnDeliveryPushClient pnDeliveryPushClient;
    private final AuditLogService auditLogService;
    private static final String ERROR_MESSAGE_NOTIFICATION_HISTORY = "errorReason = {}, An error occurred while call service for obtain notification history";
    private static final String ERROR_MESSAGE_SENT_NOTIFICATIONS = "errorReason = {}, An error occurred while calling the service to obtain sent notifications";
    private static final String ERROR_MESSAGE_SEARCH_NOTIFICATIONS = "errorReason = {}, An error occurred while calling the service to obtain sent notifications";

    public NotificationAndMessageServiceImpl(PnDataVaultClient dataVaultClient, PnDeliveryClient pnDeliveryClient, PnDeliveryPushClient pnDeliveryPushClient, AuditLogService auditLogService) {
        this.dataVaultClient = dataVaultClient;
        this.pnDeliveryClient = pnDeliveryClient;
        this.pnDeliveryPushClient = pnDeliveryPushClient;
        this.auditLogService = auditLogService;
    }

    @Override
    public Mono<SearchNotificationsResponse> searchNotificationsFromTaxId(String xPagopaPnUid, OffsetDateTime startDate, OffsetDateTime endDate, Integer size, String nextPagesKey, SearchNotificationsRequest request) {
        SearchNotificationsResponse response = new SearchNotificationsResponse();
        PnAuditLogEvent logEvent = auditLogService.buildAuditLogEvent(PnAuditLogEventType.AUD_CA_SEARCH_NOTIFICATION, "searchNotificationsFromTaxId for taxId = {}", request.getTaxId());
        return dataVaultClient.anonymized(request.getTaxId(), request.getRecipientType().getValue())
                .flatMap(internalId ->
                        pnDeliveryClient.searchNotificationsPrivate(startDate, endDate, internalId, null, null, null, size, nextPagesKey)
                                .onErrorResume(WebClientResponseException.class, exception -> {
                                    log.error(ERROR_MESSAGE_SEARCH_NOTIFICATIONS, exception.getMessage());
                                    logEvent.generateFailure(ERROR_MESSAGE_SEARCH_NOTIFICATIONS, exception.getMessage()).log();
                                    return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_CLIENT, exception.getStatusCode()));
                                })
                )
                .flatMapMany(notificationSearchResponseDto -> getNotificationSearchRowFlux(notificationSearchResponseDto, response))
                .flatMap(notificationSearchRowDto ->
                        this.pnDeliveryPushClient.getNotificationHistory(notificationSearchRowDto.getIun(), notificationSearchRowDto.getRecipients().size(), notificationSearchRowDto.getSentAt())
                                .onErrorResume(WebClientResponseException.class, exception -> {
                                    log.error("An error occurred while call service for obtain notification history: ", exception);
                                    logEvent.generateFailure(ERROR_MESSAGE_NOTIFICATION_HISTORY, exception.getMessage()).log();
                                    return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_PUSH_CLIENT, exception.getStatusCode()));
                                })
                                .map(notificationHistoryResponseDto -> NotificationAndMessageMapper
                                        .getNotification(notificationSearchRowDto, getFilteredElements(notificationHistoryResponseDto, TimelineElementCategoryV20Dto.SEND_COURTESY_MESSAGE, getIndexTaxId(request.getTaxId(), notificationSearchRowDto.getRecipients())))
                                )
                )
                .collectList()
                .map(notifications -> {
                    response.setResults(notifications);
                    logEvent.generateSuccess("searchNotificationsFromTaxId response = {}", response).log();
                    return response;
                });
    }

    private Integer getIndexTaxId(String taxId, List<String> recipients) {
        return recipients.indexOf(taxId);
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
    private static List<TimelineElementV20Dto> getFilteredElements(NotificationHistoryResponseDto notificationHistoryResponseDto, TimelineElementCategoryV20Dto category,  Integer indexTaxId) {
        List<TimelineElementV20Dto> filteredElements = new ArrayList<>();
        if (notificationHistoryResponseDto.getTimeline() != null) {
            filteredElements = notificationHistoryResponseDto.getTimeline()
                    .stream()
                    .filter(element -> {
                        if(element.getCategory() != null && element.getDetails() != null){
                            if (category == null){
                                if (element.getDetails().getRecIndex() == null){
                                    return true;
                                }
                                return element.getDetails().getRecIndex().equals(indexTaxId);
                            }
                            return element.getCategory().equals(category) &&
                                    element.getDetails().getRecIndex().equals(indexTaxId);
                        }
                        return false;
                    })
                    .toList();
        }
        return filteredElements;
    }

    @Override
    public Mono<TimelineResponse> getTimelineOfIUN(String xPagopaPnUid, String iun, SearchNotificationsRequest searchNotificationsRequest) {
        String logInfo = searchNotificationsRequest == null ? "getTimelineOfIUN for " : "getTimelineOfIUNAndTaxId for ";
        PnAuditLogEvent logEvent = auditLogService.buildAuditLogEvent(iun, PnAuditLogEventType.AUD_CA_VIEW_NOTIFICATION, logInfo);
        return pnDeliveryClient.getSentNotificationPrivate(iun)
                .onErrorResume(WebClientResponseException.class, exception -> {
                    log.error("An error occurred while calling the service to obtain sent notifications: ", exception);
                    logEvent.generateFailure(ERROR_MESSAGE_SENT_NOTIFICATIONS, exception.getMessage()).log();
                    return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_CLIENT, exception.getStatusCode()));
                })
                .flatMap(sentNotificationV21Dto -> {
                    if (searchNotificationsRequest != null){
                        return checkTaxId(sentNotificationV21Dto, searchNotificationsRequest.getTaxId());
                    }
                    return Mono.just(sentNotificationV21Dto);
                })
                .flatMap(sentNotificationV21Dto ->
                        pnDeliveryPushClient.getNotificationHistory(iun, sentNotificationV21Dto.getRecipients().size(), sentNotificationV21Dto.getSentAt())
                                .onErrorResume(WebClientResponseException.class, exception -> {
                                    log.error("An error occurred while call service for obtain notification history: ", exception);
                                    logEvent.generateFailure(ERROR_MESSAGE_NOTIFICATION_HISTORY, exception.getMessage()).log();
                                    return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_PUSH_CLIENT, exception.getStatusCode()));
                                })
                                .flatMap(notificationHistoryResponse -> filterElementFromTaxId(notificationHistoryResponse, searchNotificationsRequest, sentNotificationV21Dto))
                                .map(historyResponseDto -> {
                                    TimelineResponse response = NotificationAndMessageMapper.getTimeline(historyResponseDto);
                                    logEvent.generateSuccess(logInfo.concat("response = {}"), response).log();
                                    return response;
                                })
                );
    }

    private Mono<SentNotificationV23Dto> checkTaxId(SentNotificationV23Dto sentNotificationV21Dto, String taxId) {
        boolean taxIdMatch = sentNotificationV21Dto.getRecipients()
                .stream()
                .anyMatch(notificationRecipientV21Dto -> notificationRecipientV21Dto.getTaxId().equalsIgnoreCase(taxId));

        if (taxIdMatch) {
            return Mono.just(sentNotificationV21Dto);
        } else {
            return Mono.error(new PnGenericException(TAX_ID_NOT_FOUND, HttpStatus.NOT_FOUND));
        }
    }

    private Mono<NotificationHistoryResponseDto> filterElementFromTaxId(NotificationHistoryResponseDto response, SearchNotificationsRequest request, SentNotificationV23Dto sentNotificationV21Dto) {
        if (request == null) return Mono.just(response);
        return getIndexTaxIdFromSentNotification(request.getTaxId(), sentNotificationV21Dto.getRecipients())
                .map(indexTaxId -> {
            List<TimelineElementV20Dto> list = getFilteredElements(response, null, indexTaxId);
            response.setTimeline(list);
            return response;
        });
    }

    public Mono<Integer> getIndexTaxIdFromSentNotification(String taxId, List<NotificationRecipientV23Dto> notificationRecipientV21Dto) {
        return Flux.fromIterable(notificationRecipientV21Dto)
                .index()
                .filter(tuple -> tuple.getT2().getTaxId().equals(taxId))
                .map(Tuple2::getT1)
                .next()
                .map(index -> index != null ? index.intValue() : -1);
    }

    @Override
    public Mono<DocumentsResponse> getDocumentsOfIun(String iun, DocumentsRequest request) {
        PnAuditLogEvent logEvent = auditLogService.buildAuditLogEvent(iun, PnAuditLogEventType.AUD_CA_DOC_AVAILABLE, "getDocumentsOfIun for");
        DocumentsResponse response = new DocumentsResponse();
        AtomicInteger documentsSize = new AtomicInteger(0);
        return dataVaultClient.anonymized(request.getTaxId(), request.getRecipientType().getValue())
                .zipWhen(internalId -> pnDeliveryClient.getSentNotificationPrivate(iun)
                        .switchIfEmpty(Mono.empty())
                        .onErrorResume(WebClientResponseException.class, exception -> {
                            log.error(ERROR_MESSAGE_SENT_NOTIFICATIONS, exception.getMessage());
                            logEvent.generateFailure(ERROR_MESSAGE_SENT_NOTIFICATIONS, exception.getMessage()).log();
                            return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_CLIENT, exception.getStatusCode()));
                        })
                )
                .flatMapMany(internalIdAndSentNotificationV23Dto ->
                        getDocuments(iun, internalIdAndSentNotificationV23Dto, response, documentsSize)
                )
                .doOnError(exception -> logEvent.generateFailure("errorReason = {}, An error occurred while calling the service to obtain notification document", exception.getMessage()).log())
                .switchIfEmpty(Mono.empty())
                .collectList()
                .map(documentList -> {
                    response.setDocuments(documentList);
                    response.setTotalSize(documentsSize.get());
                    logEvent.generateSuccess("getDocumentsOfIun response = {}", response).log();
                    return response;
                });
    }

    @NotNull
    private Flux<Document> getDocuments(String iun, Tuple2<String, SentNotificationV23Dto> internalIdAndSentNotificationV23Dto, DocumentsResponse response, AtomicInteger documentsSize) {
        if (!isNotificationCancelled(internalIdAndSentNotificationV23Dto.getT2(), iun)) {
            response.setDocumentsAvailable(true);
            return Flux.fromIterable(internalIdAndSentNotificationV23Dto.getT2().getDocuments())
                    .flatMap(notificationDocumentDto ->
                            pnDeliveryClient.getReceivedNotificationDocumentPrivate(iun, Integer.parseInt(notificationDocumentDto.getDocIdx()), internalIdAndSentNotificationV23Dto.getT1(), null)
                                    .switchIfEmpty(Mono.empty())
                                    .onErrorResume(WebClientResponseException.class, exception -> {
                                        log.error("errorReason = {}, An error occurred while calling the service to obtain notification document", exception.getMessage());
                                        return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_CLIENT, exception.getStatusCode()));
                                    }))
                    .map(notificationAttachmentDownloadMetadataResponseDto -> {
                        documentsSize.set(documentsSize.get() + notificationAttachmentDownloadMetadataResponseDto.getContentLength());
                        return NotificationAndMessageMapper.getDocument(notificationAttachmentDownloadMetadataResponseDto);
                    });
        }
        return Flux.empty();
    }

    @Override
    public Mono<NotificationDetailResponse> getNotificationFromIUN(String iun) {
        PnAuditLogEvent logEvent = auditLogService.buildAuditLogEvent(iun, PnAuditLogEventType.AUD_CA_VIEW_NOTIFICATION, "getNotificationFromIUN for");
        return this.pnDeliveryClient.getSentNotificationPrivate(iun)
                .onErrorResume(WebClientResponseException.class, exception -> {
                    log.error("An error occurred while calling the service to obtain sent notifications: ", exception);
                    logEvent.generateFailure(ERROR_MESSAGE_SENT_NOTIFICATIONS, exception.getMessage()).log();
                    return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_CLIENT, exception.getStatusCode()));
                })
                .map(sentNotificationV21Dto -> {
                    NotificationDetailResponse response = NotificationAndMessageMapper.getNotificationDetail(sentNotificationV21Dto);
                    logEvent.generateSuccess("getNotificationFromIUN response = {}", response).log();
                    return response;
                });
    }

    @Override
    public Mono<SearchNotificationsResponse> searchNotificationsAsDelegateFromInternalId(String xPagopaPnUid, String mandateId, String delegateInternalId, RecipientType recipientType, Integer size, String nextPagesKey, OffsetDateTime startDate, OffsetDateTime endDate) {
        PnAuditLogEvent logEvent = auditLogService.buildAuditLogEvent(PnAuditLogEventType.AUD_CA_SEARCH_NOTIFICATION, "searchNotificationsAsDelegateFromInternalId for delegateInternalId = {}", delegateInternalId);
        SearchNotificationsResponse searchNotificationsResponse = new SearchNotificationsResponse();
        return pnDeliveryClient.searchNotificationsPrivate(startDate, endDate, delegateInternalId, null, mandateId, recipientType.getValue(), size, nextPagesKey)
                .onErrorResume(WebClientResponseException.class, exception -> {
                    log.error(ERROR_MESSAGE_SEARCH_NOTIFICATIONS, exception.getMessage());
                    logEvent.generateFailure(ERROR_MESSAGE_SEARCH_NOTIFICATIONS, exception.getMessage()).log();
                    return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_CLIENT, exception.getStatusCode()));
                })
                .flatMapMany(notificationSearchResponseDto -> getNotificationSearchRowFlux(notificationSearchResponseDto, searchNotificationsResponse))
                .map(notificationSearchResponseDto -> NotificationAndMessageMapper
                                .getNotification(notificationSearchResponseDto, null))
                .collectList()
                .map(notificationResponses -> {
                    searchNotificationsResponse.setResults(notificationResponses);
                    logEvent.generateSuccess("searchNotificationsAsDelegateFromInternalId response = {}", searchNotificationsResponse).log();
                    return searchNotificationsResponse;
                });
    }

    private boolean isNotificationCancelled(SentNotificationV23Dto sentNotificationV21Dto, String iun) {
        AtomicBoolean cancellationTimelineIsPresent = new AtomicBoolean();
        return pnDeliveryPushClient.getNotificationHistory(iun, sentNotificationV21Dto.getRecipients().size(), sentNotificationV21Dto.getSentAt())
                .onErrorResume(WebClientResponseException.class, exception -> {
                    log.error(ERROR_MESSAGE_NOTIFICATION_HISTORY, exception.getMessage());
                    return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_PUSH_CLIENT, exception.getStatusCode()));
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
