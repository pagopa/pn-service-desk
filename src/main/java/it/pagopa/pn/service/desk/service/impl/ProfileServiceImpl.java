package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.ProfileRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.ProfileResponse;
import it.pagopa.pn.service.desk.mapper.ProfileMapper;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault.PnDataVaultClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.mandate.MandateClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.userattributes.PnUserAttributesClient;
import it.pagopa.pn.service.desk.service.ProfileService;
import lombok.CustomLog;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Service
@CustomLog
public class ProfileServiceImpl implements ProfileService {

    private final PnDataVaultClient dataVaultClient;
    private final PnUserAttributesClient userAttributesClient;
    private final MandateClient mandateClient;

    public ProfileServiceImpl(PnDataVaultClient dataVaultClient, PnUserAttributesClient userAttributesClient, MandateClient mandateClient) {
        this.dataVaultClient = dataVaultClient;
        this.userAttributesClient = userAttributesClient;
        this.mandateClient = mandateClient;
    }

    @Override
    public Mono<ProfileResponse> getProfileFromTaxId(String xPagopaPnUid, ProfileRequest profileRequest) {
        ProfileResponse response = new ProfileResponse();
        return dataVaultClient.anonymized(profileRequest.getTaxId(), profileRequest.getRecipientType().getValue())
                .flatMap(internalId -> userAttributesClient.getLegalAddressBySender(internalId, "default")
                        .collectList()
                        .switchIfEmpty(Mono.just(Collections.emptyList()))
                        .flatMap(legalDigitalAddressDtos ->
                                userAttributesClient.getCourtesyAddressBySender(internalId, "default")
                                        .collectList()
                                        .switchIfEmpty(Mono.just(Collections.emptyList()))
                                        .map(courtesyDigitalAddressDtos -> ProfileMapper.getAddress(legalDigitalAddressDtos, courtesyDigitalAddressDtos, response))
                                        .map(profileResponse -> internalId)
                        )
                )
                .flatMap(internalId -> mandateClient.listMandatesByDelegator(internalId)
                        .collectList()
                        .switchIfEmpty(Mono.just(Collections.emptyList()))
                        .flatMap(internalMandateDelegators ->
                                mandateClient.listMandatesByDelegate(internalId)
                                        .collectList()
                                        .switchIfEmpty(Mono.just(Collections.emptyList()))
                                        .map(internalMandateDelegates -> ProfileMapper.getMandate(internalMandateDelegators, internalMandateDelegates, response))
                        )
                )
                .flatMap(this::deAnonymizedInternalId);
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
                .flatMap(profileResponse ->
                        Flux.fromIterable(profileResponse.getDelegatorMandates())
                                .flatMap(mandate -> dataVaultClient.deAnonymized(mandate.getDelegateInternalId())
                                        .map(taxId -> {
                                            mandate.setTaxId(taxId);
                                            return mandate;
                                        }))
                                .collectList()
                                .map(updatedMandates -> {
                                    profileResponse.setDelegatorMandates(updatedMandates);
                                    return profileResponse;
                                })
                );
    }
}
