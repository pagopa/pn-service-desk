package it.pagopa.pn.service.desk.externalclient.pnclient;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.generated.openapi.pnraddfsu.v1.dto.AORInquiryResponseDto;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.raddfsu.PnRaddFsuClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PnRaddFsuClientTest extends BaseTest.WithMockServer{

    @Autowired
    private PnRaddFsuClient pnRaddFsuClient;

    @Test
    void aorInquiry(){
        AORInquiryResponseDto aorInquiryResponseDto = this.pnRaddFsuClient.aorInquiry("uuid", "taxId","recipientType").block();
        Assertions.assertNotNull(aorInquiryResponseDto);
    }
}
