package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.raddfsu;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pnraddfsu.v1.api.AorDocumentInquiryApi;
import it.pagopa.pn.service.desk.generated.openapi.pnraddfsu.v1.dto.AORInquiryResponseDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@AllArgsConstructor
public class PnRaddFsuClientImpl implements PnRaddFsuClient {

    private AorDocumentInquiryApi documentInquiryApi;


    @Override
    public Mono<AORInquiryResponseDto> aorInquiry(String uuid, String taxId, String recipientType) {
        return documentInquiryApi.aorInquiry(uuid, taxId, recipientType);
    }
}
