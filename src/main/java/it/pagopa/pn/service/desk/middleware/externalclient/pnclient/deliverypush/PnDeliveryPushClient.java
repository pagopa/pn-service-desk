package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.ResponsePaperNotificationFailedDtoDto;
import reactor.core.publisher.Flux;

public interface PnDeliveryPushClient {

    Flux<ResponsePaperNotificationFailedDtoDto> paperNotificationFailed(String recipientInternalId);

}