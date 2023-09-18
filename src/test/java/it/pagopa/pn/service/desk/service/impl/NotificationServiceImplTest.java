package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.pnraddfsu.v1.dto.AORInquiryResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.pnraddfsu.v1.dto.ResponseStatusDto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationsUnreachableResponse;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.raddfsu.PnRaddFsuClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_ON_RADD_INQUIRY;
import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceImplTest extends BaseTest {
    @MockBean
    private PnRaddFsuClient raddFsuClient;
    @Autowired
    private NotificationServiceImpl service;

    private final NotificationRequest notificationRequest = new NotificationRequest();;

    @BeforeEach
    public void inizialize(){
        notificationRequest.setTaxId("1234");
    }


    @Test
    void getUnreachableNotificationWhenAorInquiryResponseResultIsTrue() {
        NotificationsUnreachableResponse notificationsUnreachableResponse = new NotificationsUnreachableResponse();
        notificationsUnreachableResponse.setNotificationsCount(1L);

        AORInquiryResponseDto aorInquiryResponseDto= new AORInquiryResponseDto();
        aorInquiryResponseDto.setResult(true);
        aorInquiryResponseDto.setStatus(new ResponseStatusDto());
        aorInquiryResponseDto.getStatus().setCode(ResponseStatusDto.CodeEnum.NUMBER_0);

        Mockito.when(raddFsuClient.aorInquiry(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(aorInquiryResponseDto));
        NotificationsUnreachableResponse response =service.getUnreachableNotification("1234", notificationRequest).block();
        assertNotNull(response);
        assertNotNull(response.getNotificationsCount());
        assertEquals(response.getNotificationsCount(), notificationsUnreachableResponse.getNotificationsCount());

    }

    @Test
    void getUnreachableNotificationWhenAorInquiryResponseResultIsFalse() {
        NotificationsUnreachableResponse notificationsUnreachableResponse = new NotificationsUnreachableResponse();
        notificationsUnreachableResponse.setNotificationsCount(0L);

        AORInquiryResponseDto aorInquiryResponseDto= new AORInquiryResponseDto();
        aorInquiryResponseDto.setResult(false);
        aorInquiryResponseDto.setStatus(new ResponseStatusDto());
        aorInquiryResponseDto.getStatus().setCode(ResponseStatusDto.CodeEnum.NUMBER_99);

        Mockito.when(raddFsuClient.aorInquiry(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(aorInquiryResponseDto));
        NotificationsUnreachableResponse response =service.getUnreachableNotification("1234", notificationRequest).block();
        assertNotNull(response);
        assertNotNull(response.getNotificationsCount());
        assertEquals(response.getNotificationsCount(), notificationsUnreachableResponse.getNotificationsCount());
    }

    @Test
    void getUnreachableNotificationWhenAorInquiryResponseError() {
        PnGenericException pnGenericException = new PnGenericException(ERROR_ON_RADD_INQUIRY, ERROR_ON_RADD_INQUIRY.getMessage());

        Mockito.when(raddFsuClient.aorInquiry(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.error(pnGenericException));

        StepVerifier.create(service.getUnreachableNotification("1234", notificationRequest))
                .expectError(PnGenericException.class)
                .verify();
    }
}