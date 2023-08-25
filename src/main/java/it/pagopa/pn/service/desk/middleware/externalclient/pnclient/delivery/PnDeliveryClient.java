package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationDto;
import reactor.core.publisher.Mono;

public interface PnDeliveryClient {

    Mono<SentNotificationDto> getSentNotificationPrivate(String iun);
}
