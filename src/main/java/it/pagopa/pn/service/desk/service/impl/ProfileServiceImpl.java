package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;
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
    private static final String ERROR_MESSAGE_SENDER_LEGAL_ADDRESS = "errorReason = {}, An error occurred while call service for obtain legal address by sender";
    private static final String ERROR_MESSAGE_SENDER_COURTESY_ADDRESS = "errorReason = {}, An error occurred while call service for obtain courtesy address by sender";
    private static final String ERROR_MESSAGE_LIST_MANDATES_BY_DELEGATOR = "errorReason = {}, An error occurred while call service for obtain mandates by delegator";
    private static final String ERROR_MESSAGE_LIST_MANDATES_BY_DELEGATE = "errorReason = {}, An error occurred while call service for obtain mandates by delegate";

    public ProfileServiceImpl(PnDataVaultClient dataVaultClient, PnUserAttributesClient userAttributesClient, MandateClient mandateClient, AuditLogService auditLogService) {
        this.dataVaultClient = dataVaultClient;
        this.userAttributesClient = userAttributesClient;
        this.mandateClient = mandateClient;
        this.auditLogService = auditLogService;
    }

    @Override
    public Mono<ProfileResponse> getProfileFromTaxId(String xPagopaPnUid, ProfileRequest profileRequest) {
        auditLogService.buildAuditLogEvent(PnAuditLogEventType.AUD_NT_INSERT, "getProfileFromTaxId for taxId = {}", profileRequest.getTaxId()); //FIXME mascherare il taxID

        ProfileResponse response = new ProfileResponse();
        return dataVaultClient.anonymized(profileRequest.getTaxId(), profileRequest.getRecipientType().getValue())
                .flatMap(internalId -> getAddress(internalId, response)
                )
                .flatMap(internalId -> getMandate(internalId, response)
                )
                .flatMap(this::deAnonymizedInternalId);
        //FIXME i due flatMap, posso essere fatti in parallelo? Magari utilizzando Mono.zip() e valorizzando ProfileRequest alla fine e noi in getAddress e getMandate
        //FIXME inserire qui gli audit log di failure e quello di success
    }

    @NotNull
    private Mono<ProfileResponse> getMandate(String internalId, ProfileResponse response) {
        PnAuditLogEvent logEvent = auditLogService.buildAuditLogEvent(PnAuditLogEventType.AUD_NT_INSERT, "getMandate for internalId = {}", internalId); //FIXME da eliminare
        return mandateClient.listMandatesByDelegator(internalId)
                .collectList()
                .onErrorResume(exception -> {
                    log.error("errorReason = {}, error = {}, Error during listMandatesByDelegator", exception.getMessage(), exception); //FIXME log da correggere
                    logEvent.generateFailure(ERROR_MESSAGE_LIST_MANDATES_BY_DELEGATOR, exception.getMessage()).log(); //FIXME da eliminare
                    return Mono.error(new PnGenericException(ERROR_ON_MANDATE_CLIENT, exception.getMessage()));
                })
                .flatMap(internalMandateDelegators ->
                        mandateClient.listMandatesByDelegate(internalId)
                                .collectList()
                                .onErrorResume(exception -> {
                                    log.error("errorReason = {}, error = {}, Error during listMandatesByDelegate", exception.getMessage(), exception); //FIXME log da correggere
                                    logEvent.generateFailure(ERROR_MESSAGE_LIST_MANDATES_BY_DELEGATE, exception.getMessage()).log(); //FIXME da eliminare
                                    return Mono.error(new PnGenericException(ERROR_ON_MANDATE_CLIENT, exception.getMessage()));
                                })
                                .map(internalMandateDelegates -> ProfileMapper.getMandate(internalMandateDelegators, internalMandateDelegates, response))
                );
    }

    @NotNull
    private Mono<String> getAddress(String internalId, ProfileResponse response) {
        PnAuditLogEvent logEvent = auditLogService.buildAuditLogEvent(PnAuditLogEventType.AUD_NT_INSERT, "getAddress for internalId = {}", internalId); //FIXME da eliminare, già è presente nel chiamante
        return userAttributesClient.getLegalAddressBySender(internalId, SENDER_ID_DEFAULT)
                .onErrorResume(exception -> {
                    log.error("errorReason = {}, error = {}, Error during getLegalAddressBySender", exception.getMessage(), exception); //FIXME log sbagliato, eliminare la seconda parentesi e modificare il messaggio
                    logEvent.generateFailure(ERROR_MESSAGE_SENDER_LEGAL_ADDRESS, exception.getMessage()); //FIXME i log di autit vanno solo nel service "padre"
                    return Mono.error(new PnGenericException(ERROR_ON_USER_ATTRIBUTES_CLIENT, exception.getMessage()));
                })
                .collectList()
                .flatMap(legalDigitalAddressDtos ->
                        userAttributesClient.getCourtesyAddressBySender(internalId, SENDER_ID_DEFAULT)
                                .collectList()
                                .onErrorResume(exception -> {
                                    log.error("errorReason = {}, error = {}, Error during getCourtesyAddressBySender", exception.getMessage(), exception); //FIXME log sbagliato, eliminare la seconda parentesi e modificare il messaggio
                                    logEvent.generateFailure(ERROR_MESSAGE_SENDER_COURTESY_ADDRESS, exception.getMessage()).log(); //FIXME i log di autit vanno solo nel service "padre"
                                    return Mono.error(new PnGenericException(ERROR_ON_USER_ATTRIBUTES_CLIENT, exception.getMessage()));
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
        PnAuditLogEvent logEvent = auditLogService.buildAuditLogEvent(PnAuditLogEventType.AUD_NT_INSERT, "updateDelegatorMandates"); //FIXME da eliminare
        return Flux.fromIterable(profileResponse.getDelegatorMandates())
                .flatMap(mandate -> dataVaultClient.deAnonymized(mandate.getDelegateInternalId())
                        .map(taxId -> {
                            mandate.setTaxId(taxId);
                            return mandate;
                        }))
                .collectList()
                .map(updatedMandates -> {
                    profileResponse.setDelegatorMandates(updatedMandates);
                    logEvent.generateSuccess("getProfileFromTaxId response = {}", profileResponse).log(); //FIXME da eliminare
                    return profileResponse;
                });
    }
}
