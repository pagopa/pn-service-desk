package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.raddfsu;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pnraddfsu.v1.api.AorDocumentInquiryApi;
import it.pagopa.pn.service.desk.generated.openapi.pnraddfsu.v1.dto.AORInquiryResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class PnRaddFsuClientImpl implements PnRaddFsuClient {

    @Autowired
    private AorDocumentInquiryApi documentInquiryApi;


    @Override
    public Mono<AORInquiryResponseDto> aorInquiry(String uuid, String taxId, String recipientType) {
        return documentInquiryApi.aorInquiry(uuid, taxId, recipientType);
    }
}
