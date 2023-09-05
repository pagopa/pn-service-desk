package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.generated.openapi.pnraddfsu.v1.dto.AORInquiryResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.pnraddfsu.v1.dto.ResponseStatusDto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationsUnreachableResponse;
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
        NotificationsUnreachableResponse notificationsUnreachableResponse = new NotificationsUnreachableResponse();
        notificationsUnreachableResponse.setNotificationsCount(1L);

        AORInquiryResponseDto aorInquiryResponseDto= new AORInquiryResponseDto();
        aorInquiryResponseDto.setResult(true);
        aorInquiryResponseDto.setStatus(new ResponseStatusDto());
        aorInquiryResponseDto.getStatus().setCode(ResponseStatusDto.CodeEnum.NUMBER_0);

        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setTaxId("1234");

        Mockito.when(raddFsuClient.aorInquiry(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(aorInquiryResponseDto));
        assertEquals(service.getUnreachableNotification("1234", notificationRequest).block(), notificationsUnreachableResponse);


    }
}