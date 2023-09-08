package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault;


import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.service.desk.exception.ExceptionTypeEnum;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndatavault.v1.api.RecipientsApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndatavault.v1.dto.BaseRecipientDtoDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndatavault.v1.dto.RecipientTypeDto;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.ConnectException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

@CustomLog
@Component
@AllArgsConstructor
public class PnDataVaultClientImpl implements PnDataVaultClient {
    private RecipientsApi recipientsApi;


    @Override
    public Mono<String> anonymized(String data) {
        return this.recipientsApi.ensureRecipientByExternalId(RecipientTypeDto.PF, data)
                .onErrorResume(ex ->
                        Mono.error(new PnGenericException(ExceptionTypeEnum.DATA_VAULT_DECRYPTION_ERROR, ExceptionTypeEnum.DATA_VAULT_DECRYPTION_ERROR.getMessage()))
                );
    }

    @Override
    public Mono<String> deAnonymized(String recipientInternalId) {
        List<String> toDecode = new ArrayList<>();
        toDecode.add(recipientInternalId);
        String PN_DATA_VAULT_DESCRIPTION = "Data Vault decode";
        log.logInvokingExternalService(PnLogger.EXTERNAL_SERVICES.PN_DATA_VAULT, PN_DATA_VAULT_DESCRIPTION);
        return this.recipientsApi.getRecipientDenominationByInternalId(toDecode)
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(25))
                                .filter(throwable ->throwable instanceof TimeoutException || throwable instanceof ConnectException)
                )
                .map(BaseRecipientDtoDto::getTaxId)
                .collectList()
                .map(fiscalCodes -> fiscalCodes.get(0))
                .onErrorResume(ex -> {
                    log.error("Error {}", ex.getMessage());
                    return Mono.error(new PnGenericException(ExceptionTypeEnum.DATA_VAULT_DECRYPTION_ERROR, ExceptionTypeEnum.DATA_VAULT_DECRYPTION_ERROR.getMessage()));
                });
    }

}