package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.raddfsu;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pnraddfsu.v1.api.AorDocumentInquiryApi;
import it.pagopa.pn.service.desk.generated.openapi.pnraddfsu.v1.dto.AORInquiryResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.ConnectException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
public class PnRaddFsuClientImpl implements PnRaddFsuClient {

    @Autowired
    private AorDocumentInquiryApi documentInquiryApi;


    @Override
    public Mono<AORInquiryResponseDto> aorInquiry(String uuid, String taxId, String recipientType) {
        return documentInquiryApi.aorInquiry(uuid, taxId, recipientType)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(500))
                                .filter(throwable -> throwable instanceof TimeoutException || throwable instanceof ConnectException));
    }
}
