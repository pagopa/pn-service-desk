package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.ResponsePaperNotificationFailedDtoDto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationsUnreachableResponse;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault.PnDataVaultClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClient;
import org.junit.jupiter.api.Assertions;
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
    @MockBean
    private OperationDAO operationDAO;
    private final NotificationRequest notificationRequest = new NotificationRequest();

    @BeforeEach
    public void inizialize(){
        notificationRequest.setTaxId("1234");
    }

    @Test
    void getUnreachableNotification(){

        Mockito.when(this.pnDataVaultClient.anonymized(Mockito.any())).thenReturn(Mono.just("taxId"));
        Mockito.when(this.operationDAO.searchOperationsFromRecipientInternalId(Mockito.any())).thenReturn(Flux.fromIterable(getOperations()));
        Mockito.when(this.pnDeliveryPushClient.paperNotificationFailed(Mockito.anyString())).thenReturn(Flux.fromIterable(getResponsePaperNotificationFailed()));
        NotificationsUnreachableResponse notificationsUnreachableResponse = this.service.getUnreachableNotification("fkdokm", new NotificationRequest()).block();

        Assertions.assertNotNull(notificationsUnreachableResponse);
        Assertions.assertEquals(1L,notificationsUnreachableResponse.getNotificationsCount());
    }

    @Test
    void getUnreachableNotificationWhenPaperNotificationResponseResultIsTrue() {
        NotificationsUnreachableResponse notificationsUnreachableResponse = new NotificationsUnreachableResponse();
        notificationsUnreachableResponse.setNotificationsCount(1L);

        List<ResponsePaperNotificationFailedDtoDto> lst = new ArrayList<>();
        ResponsePaperNotificationFailedDtoDto responsePaperNotificationFailedDto= new ResponsePaperNotificationFailedDtoDto();
        responsePaperNotificationFailedDto.setIun("ABC");
        lst.add(responsePaperNotificationFailedDto);
        Mockito.when(this.operationDAO.searchOperationsFromRecipientInternalId(Mockito.any())).thenReturn(Flux.fromIterable(getOperations()));
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
        Mockito.when(pnDeliveryPushClient.paperNotificationFailed(Mockito.any())).thenReturn(Flux.fromIterable(lst));
        Mockito.when(this.operationDAO.searchOperationsFromRecipientInternalId(Mockito.any())).thenReturn(Flux.fromIterable(getOperations()));
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

    private List<PnServiceDeskOperations> getOperations(){
        PnServiceDeskAttachments pnServiceDeskAttachments = new PnServiceDeskAttachments();
        PnServiceDeskAttachments pnServiceDeskAttachments2 = new PnServiceDeskAttachments();
        PnServiceDeskAttachments pnServiceDeskAttachments3 = new PnServiceDeskAttachments();
        PnServiceDeskAttachments pnServiceDeskAttachments4 = new PnServiceDeskAttachments();
        PnServiceDeskOperations pnServiceDeskOperations = new PnServiceDeskOperations();
        PnServiceDeskOperations pnServiceDeskOperations2 = new PnServiceDeskOperations();
        PnServiceDeskOperations pnServiceDeskOperations3 = new PnServiceDeskOperations();
        PnServiceDeskOperations pnServiceDeskOperations4 = new PnServiceDeskOperations();
        pnServiceDeskAttachments.setIun("iun123");
        pnServiceDeskAttachments2.setIun("iun124");
        pnServiceDeskAttachments3.setIun("iun122");
        pnServiceDeskAttachments4.setIun("iun125");
        pnServiceDeskAttachments.setIsAvailable(true);
        pnServiceDeskAttachments2.setIsAvailable(true);

        List<PnServiceDeskAttachments> attachments = new ArrayList<>();
        attachments.add(pnServiceDeskAttachments);
        attachments.add(pnServiceDeskAttachments2);
        attachments.add(pnServiceDeskAttachments3);
        attachments.add(pnServiceDeskAttachments4);

        pnServiceDeskOperations.setOperationId("123");
        pnServiceDeskOperations.setStatus("KO");
        pnServiceDeskOperations.setAttachments(attachments);
        pnServiceDeskOperations2.setOperationId("1235");
        pnServiceDeskOperations2.setStatus("KO");
        pnServiceDeskOperations2.setAttachments(attachments);
        pnServiceDeskOperations3.setOperationId("1234");
        pnServiceDeskOperations3.setStatus("KO");
        pnServiceDeskOperations3.setAttachments(attachments);
        pnServiceDeskOperations4.setOperationId("1234");
        pnServiceDeskOperations4.setStatus("OK");
        pnServiceDeskOperations4.setAttachments(attachments);

        List<PnServiceDeskOperations> operations = new ArrayList<>();

        operations.add(pnServiceDeskOperations);
        operations.add(pnServiceDeskOperations2);
        operations.add(pnServiceDeskOperations3);
        operations.add(pnServiceDeskOperations4);

        return operations;
    }

    private List<ResponsePaperNotificationFailedDtoDto> getResponsePaperNotificationFailed(){
        List<ResponsePaperNotificationFailedDtoDto> list = new ArrayList<>();
        ResponsePaperNotificationFailedDtoDto dto = new ResponsePaperNotificationFailedDtoDto();
        dto.setIun("iun123");
        list.add(dto);
        return list;
    }
}