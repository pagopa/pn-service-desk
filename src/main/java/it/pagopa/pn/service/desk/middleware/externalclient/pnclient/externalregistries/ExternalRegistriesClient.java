package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.externalregistries;


import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.payment.v1.dto.PaymentInfoRequestDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.payment.v1.dto.PaymentInfoV21Dto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.v1.dto.PaSummaryDto;
import reactor.core.publisher.Flux;

import java.util.List;


public interface ExternalRegistriesClient {
    Flux<PaSummaryDto> listOnboardedPa(String paNameFilter);
    Flux<PaymentInfoV21Dto> getPaymentInfo(List<PaymentInfoRequestDto> paymentInfoV21Dtos);

}
