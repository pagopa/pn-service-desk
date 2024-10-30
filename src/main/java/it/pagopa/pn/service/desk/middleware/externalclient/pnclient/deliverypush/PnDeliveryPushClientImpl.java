package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.api.EventComunicationApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.api.LegalFactsPrivateApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.api.PaperNotificationFailedApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.api.TimelineAndStatusApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.*;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
@CustomLog
@AllArgsConstructor
public class PnDeliveryPushClientImpl implements PnDeliveryPushClient{
    private PaperNotificationFailedApi notificationFailedApi;
    private LegalFactsPrivateApi legalFactsPrivateApi;
    private EventComunicationApi eventComunicationApi;
    private TimelineAndStatusApi timelineAndStatusApi;

    private static final String RADD_TYPE = "SERVICE_DESK";

    @Override
    public Flux<ResponsePaperNotificationFailedDtoDto> paperNotificationFailed(String recipientInternalId) {
        return notificationFailedApi.paperNotificationFailed(recipientInternalId, Boolean.FALSE)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode() == HttpStatus.NOT_FOUND){
                        log.warn("No notification failed found for {}", recipientInternalId);
                        return Flux.empty();
                    }
                    return Mono.error(ex);
                });
    }

    @Override
    public Flux<LegalFactListElementDto> getNotificationLegalFactsPrivate(String recipientInternalId, String iun) {
        return legalFactsPrivateApi.getNotificationLegalFactsPrivate(recipientInternalId,iun, null,null,null);
    }

    @Override
    public Mono<ResponseNotificationViewedDtoDto> notifyNotificationViewed(String iun, String operationId, String internalRecipientId) {
        RequestNotificationViewedDtoDto request = new RequestNotificationViewedDtoDto();
        request.setRecipientType(RecipientTypeDto.PF);
        request.setRecipientInternalId(internalRecipientId);
        request.setRaddBusinessTransactionDate(Instant.now());
        request.setRaddBusinessTransactionId(operationId);
        request.setRaddType(RADD_TYPE);
        return this.eventComunicationApi.notifyNotificationViewed(iun, request)
                .map(item -> {
                    log.info("response of notification viewed : {}", item.getIun());
                    return item;
                })
                .onErrorResume(ex -> {
                    log.error("Notification viewed in error {}", ex);
                    return Mono.empty();
                });
    }

    @Override
    public Mono<NotificationHistoryResponseDto> getNotificationHistory(String iun, Integer numberOfRecipients, Instant createdAt) {
        return this.timelineAndStatusApi.getNotificationHistory(iun, numberOfRecipients, createdAt);
    }
}
