package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
import it.pagopa.pn.commons.utils.LogUtils;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.ProfileRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.ProfileResponse;
import it.pagopa.pn.service.desk.mapper.ProfileMapper;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault.PnDataVaultClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.mandate.MandateClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.userattributes.PnUserAttributesClient;
import it.pagopa.pn.service.desk.service.AuditLogService;
import it.pagopa.pn.service.desk.service.ProfileService;
import lombok.CustomLog;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_ON_MANDATE_CLIENT;
import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_ON_USER_ATTRIBUTES_CLIENT;

@Service
@CustomLog
public class ProfileServiceImpl implements ProfileService {

    private final PnDataVaultClient dataVaultClient;
    private final PnUserAttributesClient userAttributesClient;
    private final MandateClient mandateClient;
    public static final String SENDER_ID_DEFAULT = "default";
    private final AuditLogService auditLogService;
    private static final String ERROR_MESSAGE_LIST_MANDATES = "An error occurred while call service for obtain mandates by delegate: ";

    public ProfileServiceImpl(PnDataVaultClient dataVaultClient, PnUserAttributesClient userAttributesClient, MandateClient mandateClient, AuditLogService auditLogService) {
        this.dataVaultClient = dataVaultClient;
        this.userAttributesClient = userAttributesClient;
        this.mandateClient = mandateClient;
        this.auditLogService = auditLogService;
    }

    @Override
    public Mono<ProfileResponse> getProfileFromTaxId(String xPagopaPnUid, ProfileRequest profileRequest) {
        PnAuditLogEvent logEvent = auditLogService.buildAuditLogEvent(PnAuditLogEventType.AUD_CA_VIEW_USERPROFILE, "getProfileFromTaxId for taxId = {}", LogUtils.maskTaxId(profileRequest.getTaxId()));

        ProfileResponse response = new ProfileResponse();
        return dataVaultClient.anonymized(profileRequest.getTaxId(), profileRequest.getRecipientType().getValue())
                .flatMap(internalId ->
                        Mono.zip(getAddress(internalId, response), getMandate(internalId, response), (a, b) -> b)
                )
                .doOnError(exception -> logEvent.generateFailure(ERROR_MESSAGE_LIST_MANDATES, exception).log())
                .doOnSuccess(profileResponse -> logEvent.generateSuccess("profileResponse = {}", profileResponse).log())
                .flatMap(this::deAnonymizedInternalId);
    }

    @NotNull
    private Mono<ProfileResponse> getMandate(String internalId, ProfileResponse response) {
        return mandateClient.listMandatesByDelegator(internalId)
                .collectList()
                .onErrorResume(WebClientResponseException.class, exception -> {
                    log.error("errorReason = {}, Error during listMandatesByDelegator", exception.getMessage(), exception);
                    return Mono.error(new PnGenericException(ERROR_ON_MANDATE_CLIENT, exception.getStatusCode()));
                })
                .flatMap(internalMandateDelegators ->
                        mandateClient.listMandatesByDelegate(internalId)
                                .collectList()
                                .onErrorResume(WebClientResponseException.class, exception -> {
                                    log.error("errorReason = {}, Error during listMandatesByDelegate", exception.getMessage(), exception);
                                    return Mono.error(new PnGenericException(ERROR_ON_MANDATE_CLIENT, exception.getStatusCode()));
                                })
                                .map(internalMandateDelegates -> ProfileMapper.getMandate(internalMandateDelegators, internalMandateDelegates, response))
                );
    }

    @NotNull
    private Mono<String> getAddress(String internalId, ProfileResponse response) {
        return userAttributesClient.getLegalAddressBySender(internalId, SENDER_ID_DEFAULT)
                .onErrorResume(WebClientResponseException.class, exception -> {
                    log.error("errorReason = {}, Error during getLegalAddressBySender", exception.getMessage(), exception);
                    return Mono.error(new PnGenericException(ERROR_ON_USER_ATTRIBUTES_CLIENT, exception.getStatusCode()));
                })
                .collectList()
                .flatMap(legalDigitalAddressDtos ->
                        userAttributesClient.getCourtesyAddressBySender(internalId, SENDER_ID_DEFAULT)
                                .collectList()
                                .onErrorResume(WebClientResponseException.class, exception -> {
                                    log.error("errorReason = {}, Error during getCourtesyAddressBySender", exception.getMessage(), exception);
                                    return Mono.error(new PnGenericException(ERROR_ON_USER_ATTRIBUTES_CLIENT, exception.getStatusCode()));
                                })
                                .map(courtesyDigitalAddressDtos -> ProfileMapper.getAddress(legalDigitalAddressDtos, courtesyDigitalAddressDtos, response))
                                .map(profileResponse -> internalId)
                );
    }

    private Mono<ProfileResponse> deAnonymizedInternalId(ProfileResponse response) {
        return Flux.fromIterable(response.getDelegateMandates())
                .flatMap(mandate -> dataVaultClient.deAnonymized(mandate.getDelegatorInternalId())
                        .map(taxId -> {
                            mandate.setTaxId(taxId);
                            return mandate;
                        }))
                .collectList()
                .map(updatedMandates -> {
                    response.setDelegateMandates(updatedMandates);
                    return response;
                })
                .flatMap(this::updateDelegatorMandates);
    }

    @NotNull
    private Mono<ProfileResponse> updateDelegatorMandates(ProfileResponse profileResponse) {
        return Flux.fromIterable(profileResponse.getDelegatorMandates())
                .flatMap(mandate -> dataVaultClient.deAnonymized(mandate.getDelegateInternalId())
                        .map(taxId -> {
                            mandate.setTaxId(taxId);
                            return mandate;
                        }))
                .collectList()
                .map(updatedMandates -> {
                    profileResponse.setDelegatorMandates(updatedMandates);
                    return profileResponse;
                });
    }
}
