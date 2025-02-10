package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.externalregistries;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.payment.v1.api.PaymentInfoApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.payment.v1.dto.PaymentInfoRequestDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.payment.v1.dto.PaymentInfoV21Dto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.v1.api.InfoPaApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.v1.dto.PaSummaryDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.v1.dto.PaSummaryExtendedResponseDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;


@Slf4j
@Component
@AllArgsConstructor
public class ExternalRegistriesClientImpl implements ExternalRegistriesClient {

    private InfoPaApi infoPaApi;
    private PaymentInfoApi paymentInfoApi;


    @Override
    public Flux<PaSummaryDto> listOnboardedPa(String paNameFilter) {
        return infoPaApi.listOnboardedPa(paNameFilter, null);
    }

    /**
     * Retrieves a paginated list of onboarded public administrations (PA) that match the given filter criteria.
     * This method supports filtering by name and selecting only child institutions.
     *
     * The method calls the `extendedListOnboardedPa` endpoint of the `InfoPaApi` to fetch the requested data.
     *
     * @param paNameFilter  An optional filter string to match PA names (can be null or empty for no filtering).
     * @param onlyChildren  If true, retrieves only child institutions; otherwise, retrieves both parents and children.
     * @param page          The page number to retrieve (1-based index). If null, defaults to the first page.
     * @param size          The number of items per page. If null, a default size is used.
     * @return A {@link Mono} emitting a {@link PaSummaryExtendedResponseDto} containing the paginated list of PA.
     */
    @Override
    public Mono<PaSummaryExtendedResponseDto> extendedListOnboardedPa(String paNameFilter, Boolean onlyChildren, Integer page, Integer size) {
        return infoPaApi.extendedListOnboardedPa(paNameFilter, onlyChildren, page, size);
    }

    public Flux<PaymentInfoV21Dto> getPaymentInfo(List<PaymentInfoRequestDto> paymentInfoV21Dtos) {
        return paymentInfoApi.getPaymentInfoV21(paymentInfoV21Dtos);
    }
}