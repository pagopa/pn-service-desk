package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.raddfsu;

import it.pagopa.pn.service.desk.generated.openapi.pnraddfsu.v1.dto.AORInquiryResponseDto;
import reactor.core.publisher.Mono;

public interface PnRaddFsuClient {

    Mono<AORInquiryResponseDto> aorInquiry (String uuid, String taxId, String recipientType);
}
