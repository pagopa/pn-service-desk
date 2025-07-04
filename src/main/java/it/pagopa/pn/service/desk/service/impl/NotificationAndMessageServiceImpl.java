package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationRecipientV24Dto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationSearchResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationSearchRowDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationV25Dto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.NotificationHistoryResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.TimelineElementCategoryV27Dto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.TimelineElementV27Dto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.payment.v1.dto.PaymentInfoRequestDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.payment.v1.dto.PaymentInfoV21Dto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.service.desk.mapper.NotificationAndMessageMapper;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault.PnDataVaultClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.externalregistries.ExternalRegistriesClient;
import it.pagopa.pn.service.desk.service.AuditLogService;
import it.pagopa.pn.service.desk.service.NotificationAndMessageService;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.*;

@Service
@CustomLog
@RequiredArgsConstructor
public class NotificationAndMessageServiceImpl implements NotificationAndMessageService {

    private final PnDataVaultClient dataVaultClient;
    private final PnDeliveryClient pnDeliveryClient;
    private final PnDeliveryPushClient pnDeliveryPushClient;
    private final ExternalRegistriesClient externalRegistriesClient;
    private final AuditLogService auditLogService;
    private static final String ERROR_MESSAGE_NOTIFICATION_HISTORY = "errorReason = {}, An error occurred while call service for obtain notification history";
    private static final String ERROR_MESSAGE_SENT_NOTIFICATIONS = "errorReason = {}, An error occurred while calling the service to obtain sent notifications";
    private static final String ERROR_MESSAGE_SEARCH_NOTIFICATIONS = "errorReason = {}, An error occurred while calling the service to obtain sent notifications";


    @Override
    public Mono<SearchNotificationsResponse> searchNotificationsFromTaxId(String xPagopaPnUid, Instant startDate, Instant endDate, Integer size, String nextPagesKey, SearchNotificationsRequest request) {
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
                                        .getNotification(notificationSearchRowDto, getFilteredElements(notificationHistoryResponseDto, TimelineElementCategoryV27Dto.SEND_COURTESY_MESSAGE, getIndexTaxId(request.getTaxId(), notificationSearchRowDto.getRecipients())))
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
    private static List<TimelineElementV27Dto> getFilteredElements(NotificationHistoryResponseDto notificationHistoryResponseDto, TimelineElementCategoryV27Dto category,  Integer indexTaxId) {
        List<TimelineElementV27Dto> filteredElements = new ArrayList<>();
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

    private Mono<SentNotificationV25Dto> checkTaxId(SentNotificationV25Dto sentNotificationV21Dto, String taxId) {
        boolean taxIdMatch = sentNotificationV21Dto.getRecipients()
                .stream()
                .anyMatch(notificationRecipientV21Dto -> notificationRecipientV21Dto.getTaxId().equalsIgnoreCase(taxId));

        if (taxIdMatch) {
            return Mono.just(sentNotificationV21Dto);
        } else {
            return Mono.error(new PnGenericException(TAX_ID_NOT_FOUND, HttpStatus.NOT_FOUND));
        }
    }

    private Mono<NotificationHistoryResponseDto> filterElementFromTaxId(NotificationHistoryResponseDto response, SearchNotificationsRequest request, SentNotificationV25Dto sentNotificationV21Dto) {
        if (request == null) return Mono.just(response);
        return getIndexTaxIdFromSentNotification(request.getTaxId(), sentNotificationV21Dto.getRecipients())
                .map(indexTaxId -> {
            var filteredTimelines = getFilteredElements(response, null, indexTaxId);
            response.setTimeline(filteredTimelines);
            return response;
        });
    }

    public Mono<Integer> getIndexTaxIdFromSentNotification(String taxId, List<NotificationRecipientV24Dto> notificationRecipientV21Dto) {
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
    private Flux<Document> getDocuments(String iun, Tuple2<String, SentNotificationV25Dto> internalIdAndSentNotificationV23Dto, DocumentsResponse response, AtomicInteger documentsSize) {
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
        return callSentNotificationPrivate(iun, logEvent)
                .map(sentNotificationV21Dto -> {
                    NotificationDetailResponse response = NotificationAndMessageMapper.getNotificationDetail(sentNotificationV21Dto);
                    logEvent.generateSuccess("getNotificationFromIUN response = {}", response).log();
                    return response;
                });
    }

    @Override
    public Mono<SearchNotificationsResponse> searchNotificationsAsDelegateFromInternalId(String xPagopaPnUid, String mandateId, String delegateInternalId, RecipientType recipientType, Integer size, String nextPagesKey, Instant startDate, Instant endDate) {
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

    private boolean isNotificationCancelled(SentNotificationV25Dto sentNotificationV21Dto, String iun) {
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
        var cancellationRequestCategory = TimelineElementCategoryV27Dto.NOTIFICATION_CANCELLATION_REQUEST;
        var cancellationRequestTimeline = notificationHistoryResponseDto.getTimeline().stream()
                .filter(timelineElement -> cancellationRequestCategory.toString().equals(timelineElement.getCategory().toString()))
                .findFirst();
        cancellationTimelineIsPresent.set(cancellationRequestTimeline.isPresent());
        if (cancellationTimelineIsPresent.get()) {
            log.warn("Notification with iun: {} has a request for cancellation", iun);
        }
        return cancellationTimelineIsPresent.get();
    }

    @Override
    public Mono<NotificationRecipientDetailResponse> getNotificationRecipientDetail(String iun, String recipientTaxId) {
        PnAuditLogEvent logEvent = auditLogService.buildAuditLogEvent(iun, PnAuditLogEventType.AUD_CA_VIEW_NOTIFICATION, "getNotificationRecipientDetail for");
        return callSentNotificationPrivate(iun, logEvent)
                .map(sentNotificationV21Dto -> NotificationAndMessageMapper.getNotificationRecipientDetailResponse(sentNotificationV21Dto, recipientTaxId))
                .flatMap(this::enrichWithPaymentsDetail)
                .doOnNext(response -> logEvent.generateSuccess("getNotificationRecipientDetail response = {}", response).log());
    }

    private Mono<SentNotificationV25Dto> callSentNotificationPrivate(String iun, PnAuditLogEvent logEvent) {
        return this.pnDeliveryClient.getSentNotificationPrivate(iun)
                .onErrorResume(WebClientResponseException.class, exception -> {
                    log.error("An error occurred while calling the service to obtain sent notifications: ", exception);
                    logEvent.generateFailure(ERROR_MESSAGE_SENT_NOTIFICATIONS, exception.getMessage()).log();
                    return Mono.error(new PnGenericException(ERROR_ON_DELIVERY_CLIENT, exception.getStatusCode()));
                });
    }

    private Mono<NotificationRecipientDetailResponse> enrichWithPaymentsDetail(NotificationRecipientDetailResponse response) {
        if (Boolean.TRUE.equals(response.getHasPayments())) {
            return enrichPayment(response.getRecipient().getPayments())
                    .thenReturn(response);
        } else {
            return Mono.just(response);
        }
    }

    private Mono<Void> enrichPayment(List<NotificationPaymentItem> paymentsPagoPa) {
        var noticeCodePayment = new HashMap<String, NotificationPaymentItem>();
         var list = paymentsPagoPa.stream()
                .filter(paymentItem -> paymentItem.getPagoPa() != null)
                .map(paymentItem -> {
                    noticeCodePayment.put(paymentItem.getPagoPa().getNoticeCode(), paymentItem);
                    return new PaymentInfoRequestDto()
                            .creditorTaxId(paymentItem.getPagoPa().getCreditorTaxId())
                            .noticeCode(paymentItem.getPagoPa().getNoticeCode());
                })
                .toList();

        return externalRegistriesClient.getPaymentInfo(list)
                .onErrorResume(e -> {
                    // non faccio andare in errore l'intero flusso se non va in porto il recupero del dettaglio pagamento
                    log.error("Error in getPaymentInfo, PaymentInfoRequestDtos={}", list, e);
                    return Flux.empty();
                })
                .doOnNext(paymentInfoV21Dto -> {
                        var notificationPaymentItem = noticeCodePayment.get(paymentInfoV21Dto.getNoticeCode());
                        enrichNotificationPaymentItemWithPaymentInfo(notificationPaymentItem, paymentInfoV21Dto);
                })
                .then();
    }

    private void enrichNotificationPaymentItemWithPaymentInfo(NotificationPaymentItem paymentItem, PaymentInfoV21Dto paymentInfoV21Dto) {
        if(paymentItem != null) {
            paymentItem.getPagoPa().setAmount(paymentInfoV21Dto.getAmount());
            paymentItem.getPagoPa().setCausaleVersamento(paymentInfoV21Dto.getCausaleVersamento());
            paymentItem.getPagoPa().setDueDate(paymentInfoV21Dto.getDueDate());
            paymentItem.getPagoPa().setStatus(paymentInfoV21Dto.getStatus().getValue());
            paymentItem.getPagoPa().setErrorCode(paymentInfoV21Dto.getErrorCode());
            paymentItem.getPagoPa().setDetail(paymentInfoV21Dto.getDetail() != null ? paymentInfoV21Dto.getDetail().getValue() : null);
        }

    }

}
