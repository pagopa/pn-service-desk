package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.LegalFactListElementDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.RequestNotificationViewedDtoDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.ResponseNotificationViewedDtoDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.ResponsePaperNotificationFailedDtoDto;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;


public interface PnDeliveryPushClient {

    Flux<ResponsePaperNotificationFailedDtoDto> paperNotificationFailed(String recipientInternalId);
    Flux<LegalFactListElementDto> getNotificationLegalFactsPrivate(String recipientInternalId, String iun);
    Mono<ResponseNotificationViewedDtoDto> notifyNotificationViewed(String iun, PnServiceDeskOperations entity);

}
