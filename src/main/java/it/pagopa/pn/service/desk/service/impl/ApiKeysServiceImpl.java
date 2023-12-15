package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.ResponseApiKeys;
import it.pagopa.pn.service.desk.mapper.ApiKeysMapper;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.apikeysmanager.ApiKeysManagerClient;
import it.pagopa.pn.service.desk.service.ApiKeysService;
import it.pagopa.pn.service.desk.service.AuditLogService;
import lombok.CustomLog;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_ON_KEYS_MANAGER_CLIENT;

@Service
@CustomLog
public class ApiKeysServiceImpl implements ApiKeysService {

    private final ApiKeysManagerClient apiKeysManagerClient;
    private final AuditLogService auditLogService;

    public ApiKeysServiceImpl(ApiKeysManagerClient apiKeysManagerClient, AuditLogService auditLogService) {
        this.apiKeysManagerClient = apiKeysManagerClient;
        this.auditLogService = auditLogService;
    }

    @Override
    public Mono<ResponseApiKeys> getApiKeys(String paId) {
        PnAuditLogEvent logEvent = auditLogService.buildAuditLogEvent(PnAuditLogEventType.AUD_NT_INSERT, "getApiKeys for paId={}", paId);
        return apiKeysManagerClient.getBoApiKeys(paId)
                .onErrorResume(WebClientResponseException.class, exception -> {
                    log.error("errorReason = {}, An error occurred while calling the service to obtain api keys", exception.getMessage(), exception);
                    logEvent.generateFailure("errorReason = {}, An error occurred while calling the service to obtain api keys" + exception.getMessage()).log();
                    return Mono.error(new PnGenericException(ERROR_ON_KEYS_MANAGER_CLIENT, exception.getStatusCode()));
                }).map(responseApiKeysDto -> {
                    ResponseApiKeys responseApiKeys = ApiKeysMapper.responseApiKeys(responseApiKeysDto);
                    logEvent.generateSuccess("getApiKeys responseApiKeys = {}", responseApiKeys).log();
                    return responseApiKeys;
                });
    }
}