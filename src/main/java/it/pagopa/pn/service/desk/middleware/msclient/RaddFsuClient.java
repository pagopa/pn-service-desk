package it.pagopa.pn.service.desk.middleware.msclient;

import it.pagopa.pn.service.desk.generated.openapi.pn_radd_fsu.v1.dto.AORInquiryResponseDto;
import reactor.core.publisher.Mono;

public interface RaddFsuClient {

    Mono<AORInquiryResponseDto> aorInquiry (String uuid, String taxId, String recipientType);
}
