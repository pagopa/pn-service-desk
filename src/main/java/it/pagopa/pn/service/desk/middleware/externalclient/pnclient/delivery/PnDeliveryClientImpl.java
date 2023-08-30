package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.api.InternalOnlyApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class PnDeliveryClientImpl implements PnDeliveryClient{
    private InternalOnlyApi internalOnlyApi;

    @Override
    public Mono<SentNotificationDto> getSentNotificationPrivate(String iun) {
        return internalOnlyApi.getSentNotificationPrivate(iun);
    }
}
