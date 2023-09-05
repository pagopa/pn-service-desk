package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.generated.openapi.pnraddfsu.v1.dto.AORInquiryResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.pnraddfsu.v1.dto.ResponseStatusDto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationRequest;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.raddfsu.PnRaddFsuClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceImplTest extends BaseTest {


    @MockBean
    private PnRaddFsuClient raddFsuClient;
    @Autowired
    private NotificationServiceImpl service;



    @Test
    void getUnreachableNotification() {
        AORInquiryResponseDto aorInquiryResponseDto= new AORInquiryResponseDto();
        aorInquiryResponseDto.setResult(true);
        aorInquiryResponseDto.setStatus(new ResponseStatusDto());
        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setTaxId("1234");
        Mockito.when(raddFsuClient.aorInquiry(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(aorInquiryResponseDto));

    }
}