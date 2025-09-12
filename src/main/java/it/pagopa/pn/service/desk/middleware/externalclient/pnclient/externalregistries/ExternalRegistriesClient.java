package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.externalregistries;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.payment.v1.dto.PaymentInfoRequestDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.payment.v1.dto.PaymentInfoV21Dto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.v1.dto.PaSummaryDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.v1.dto.PaSummaryExtendedResponseDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;


public interface ExternalRegistriesClient {
    Flux<PaSummaryDto> listOnboardedPa(String paNameFilter);
    Mono<PaSummaryExtendedResponseDto> extendedListOnboardedPa(String paNameFilter, Boolean onlyChildren, Integer page, Integer size);
    Flux<PaymentInfoV21Dto> getPaymentInfo(List<PaymentInfoRequestDto> paymentInfoV21Dtos);
}