package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.externalregistries;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.payment.v1.api.PaymentInfoApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.payment.v1.dto.PaymentInfoRequestDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.payment.v1.dto.PaymentInfoV21Dto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.v1.api.InfoPaApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.v1.dto.PaSummaryDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

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

    public Flux<PaymentInfoV21Dto> getPaymentInfo(List<PaymentInfoRequestDto> paymentInfoV21Dtos) {
        return paymentInfoApi.getPaymentInfoV21(paymentInfoV21Dtos);
    }
}
