package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.ResponseApiKeys;
import it.pagopa.pn.service.desk.mapper.ApiKeysMapper;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.apikeysmanager.ApiKeysManagerClient;
import it.pagopa.pn.service.desk.service.ApiKeysService;
import lombok.CustomLog;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_ON_KEYS_MANAGER_CLIENT;

@Service
@CustomLog
public class ApiKeysServiceImpl implements ApiKeysService {

    private final ApiKeysManagerClient apiKeysManagerClient;

    private final PnAuditLogBuilder auditLogBuilder = new PnAuditLogBuilder();

    public ApiKeysServiceImpl(ApiKeysManagerClient apiKeysManagerClient) {
        this.apiKeysManagerClient = apiKeysManagerClient;
    }

    @Override
    public Mono<ResponseApiKeys> getApiKeys(String paId) {
        PnAuditLogEvent logEvent = auditLogBuilder.before(PnAuditLogEventType.AUD_NT_INSERT, "getApiKeys for paId={}", paId)
                .build().log();
        return apiKeysManagerClient.getBoApiKeys(paId).switchIfEmpty(Mono.empty()).onErrorResume(exception -> {
            log.error("errorReason = {}, An error occurred while calling the service to obtain api keys", exception.getMessage());
            logEvent.generateFailure("errorReason = {}, An error occurred while calling the service to obtain api keys" + exception.getMessage()).log();
            return Mono.error(new PnGenericException(ERROR_ON_KEYS_MANAGER_CLIENT, exception.getMessage()));
        }).flatMap(responseApiKeysDto ->{
            logEvent.generateSuccess("getApiKeys responseApiKeys: items={}, totalElement={}", responseApiKeysDto.getItems(), responseApiKeysDto.getTotal()).log();
            return Mono.just(ApiKeysMapper.responseApiKeys(responseApiKeysDto));
                });
    }
}