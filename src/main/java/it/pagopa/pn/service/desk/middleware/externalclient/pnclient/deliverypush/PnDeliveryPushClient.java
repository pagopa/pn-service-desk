package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.LegalFactListElementV28Dto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.NotificationHistoryResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.ResponseNotificationViewedDtoDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.ResponsePaperNotificationFailedDtoDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface PnDeliveryPushClient {

    Flux<ResponsePaperNotificationFailedDtoDto> paperNotificationFailed(String recipientInternalId);
    Flux<LegalFactListElementV28Dto> getNotificationLegalFactsPrivate(String recipientInternalId, String iun);
    Mono<ResponseNotificationViewedDtoDto> notifyNotificationViewed(String iun, String operationId, String internalRecipientId);
    Mono<NotificationHistoryResponseDto> getNotificationHistory(String iun, Integer numberOfRecipients, Instant createdAt);

}
