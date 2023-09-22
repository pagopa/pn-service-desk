package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.ResponsePaperNotificationFailedDtoDto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationsUnreachableResponse;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault.PnDataVaultClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_ON_RADD_INQUIRY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NotificationServiceImplTest extends BaseTest {
    @MockBean
    private PnDeliveryPushClient pnDeliveryPushClient;

    @MockBean
    private PnDataVaultClient pnDataVaultClient;
    @Autowired
    private NotificationServiceImpl service;

    private final NotificationRequest notificationRequest = new NotificationRequest();;

    @BeforeEach
    public void inizialize(){
        notificationRequest.setTaxId("1234");
    }


    @Test
    void getUnreachableNotificationWhenPaperNotificationResponseResultIsTrue() {
        NotificationsUnreachableResponse notificationsUnreachableResponse = new NotificationsUnreachableResponse();
        notificationsUnreachableResponse.setNotificationsCount(1L);

        List<ResponsePaperNotificationFailedDtoDto> lst = new ArrayList<>();
        ResponsePaperNotificationFailedDtoDto responsePaperNotificationFailedDto= new ResponsePaperNotificationFailedDtoDto();
        responsePaperNotificationFailedDto.setIun("ABC");
        lst.add(responsePaperNotificationFailedDto);

        Mockito.when(pnDeliveryPushClient.paperNotificationFailed(Mockito.any())).thenReturn(Flux.fromIterable(lst));
        Mockito.when(pnDataVaultClient.anonymized(Mockito.any())).thenReturn(Mono.just("xyz"));
        NotificationsUnreachableResponse response =service.getUnreachableNotification("1234", notificationRequest).block();
        assertNotNull(response);
        assertNotNull(response.getNotificationsCount());
        assertEquals(response.getNotificationsCount(), notificationsUnreachableResponse.getNotificationsCount());

    }

    @Test
    void getUnreachableNotificationWhenPaperNotificationResponseResultIsFalse() {
        NotificationsUnreachableResponse notificationsUnreachableResponse = new NotificationsUnreachableResponse();
        notificationsUnreachableResponse.setNotificationsCount(0L);

        List<ResponsePaperNotificationFailedDtoDto> lst = new ArrayList<>();
        Mockito.when(pnDataVaultClient.anonymized(Mockito.any())).thenReturn(Mono.just("xyz"));
        Mockito.when(pnDeliveryPushClient.paperNotificationFailed(Mockito.any()))
                .thenReturn(Flux.fromIterable(lst));
        NotificationsUnreachableResponse response =service.getUnreachableNotification("1234", notificationRequest).block();
        assertNotNull(response);
        assertNotNull(response.getNotificationsCount());
        assertEquals(response.getNotificationsCount(), notificationsUnreachableResponse.getNotificationsCount());
    }

    @Test
    void getUnreachableNotificationWhenPaperNotificationResponseError() {
        PnGenericException pnGenericException = new PnGenericException(ERROR_ON_RADD_INQUIRY, ERROR_ON_RADD_INQUIRY.getMessage());
        Mockito.when(pnDataVaultClient.anonymized(Mockito.any())).thenReturn(Mono.just("xyz"));
        Mockito.when(pnDeliveryPushClient.paperNotificationFailed(Mockito.any()))
                .thenReturn(Flux.error(pnGenericException));

        StepVerifier.create(service.getUnreachableNotification("1234", notificationRequest))
                .expectError(PnGenericException.class)
                .verify();
    }
}