package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault;


import it.pagopa.pn.service.desk.exception.ExceptionTypeEnum;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndatavault.v1.api.RecipientsApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndatavault.v1.dto.RecipientTypeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.ConnectException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class PnDataVaultClientImpl implements PnDataVaultClient {

    @Autowired
    private RecipientsApi recipientsApi;


    @Override
    public Mono<String> anonymized(String data) {

        return this.recipientsApi.ensureRecipientByExternalId(RecipientTypeDto.PF, data)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(25))
                                .filter(throwable ->throwable instanceof TimeoutException || throwable instanceof ConnectException)
                )
                .onErrorResume(ex -> {
                    log.error("Error {}", ex.getMessage());
                    return Mono.error(new PnGenericException(ExceptionTypeEnum.DATA_VAULT_DECRYPTION_ERROR, ExceptionTypeEnum.DATA_VAULT_DECRYPTION_ERROR.getMessage()));
                });
    }
}