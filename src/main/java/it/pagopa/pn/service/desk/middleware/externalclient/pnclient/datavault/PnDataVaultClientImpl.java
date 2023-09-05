package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault;


import it.pagopa.pn.service.desk.exception.ExceptionTypeEnum;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndatavault.v1.api.RecipientsApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndatavault.v1.dto.RecipientTypeDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
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
}