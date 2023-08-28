package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.api.InternalOnlyApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.ConnectException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Component
public class PnDeliveryClientImpl implements PnDeliveryClient{

    @Autowired
    private InternalOnlyApi internalOnlyApi;

    @Override
    public Mono<SentNotificationDto> getSentNotificationPrivate(String iun) {
        return internalOnlyApi.getSentNotificationPrivate(iun)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(500))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException));
    }
}
