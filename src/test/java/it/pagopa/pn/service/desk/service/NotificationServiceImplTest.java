package it.pagopa.pn.service.desk.service;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.exception.ExceptionTypeEnum;
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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_GET_UNREACHABLE_NOTIFICATION;

class NotificationServiceImplTest extends BaseTest.WithMockServer {

    @Autowired
    private NotificationService notificationService;

    @MockBean
    private PnDataVaultClient dataVaultClient;
    @MockBean
    private PnDeliveryPushClient deliveryPushClient;
    @MockBean
    private OperationDAO operationDAO;


    PnServiceDeskOperations pnServiceDeskOperations = new PnServiceDeskOperations();
    PnServiceDeskOperations pnServiceDeskOperations2 = new PnServiceDeskOperations();
    PnServiceDeskOperations pnServiceDeskOperations3 = new PnServiceDeskOperations();
    PnServiceDeskOperations pnServiceDeskOperations4 = new PnServiceDeskOperations();
    PnServiceDeskAttachments pnServiceDeskAttachments = new PnServiceDeskAttachments();
    PnServiceDeskAttachments pnServiceDeskAttachments2 = new PnServiceDeskAttachments();
    PnServiceDeskAttachments pnServiceDeskAttachments3 = new PnServiceDeskAttachments();
    PnServiceDeskAttachments pnServiceDeskAttachments4 = new PnServiceDeskAttachments();



    @Test
    void getUnreachableNotification(){

        Mockito.when(this.dataVaultClient.anonymized(Mockito.any())).thenReturn(Mono.just("taxId"));
        Mockito.when(this.operationDAO.searchOperationsFromRecipientInternalId(Mockito.any())).thenReturn(Flux.fromIterable(getOperations()));
        Mockito.when(this.deliveryPushClient.paperNotificationFailed(Mockito.anyString())).thenReturn(Flux.fromIterable(getResponsePaperNotificationFailed()));
        NotificationsUnreachableResponse notificationsUnreachableResponse = this.notificationService.getUnreachableNotification("fkdokm", new NotificationRequest()).block();

        Assertions.assertNotNull(notificationsUnreachableResponse);
        Assertions.assertEquals(1L,notificationsUnreachableResponse.getNotificationsCount());

    }

    @Test
    void getUnreachableNotification_No_Notification_Unreachable(){
        Mockito.when(this.dataVaultClient.anonymized(Mockito.any())).thenReturn(Mono.just("taxId"));
        Mockito.when(this.deliveryPushClient.paperNotificationFailed(Mockito.anyString())).thenReturn(Flux.empty());
        Mockito.when(this.operationDAO.searchOperationsFromRecipientInternalId(Mockito.any())).thenReturn(Flux.fromIterable(getOperationsNoUnreachable()));

        NotificationsUnreachableResponse notificationsUnreachableResponse = this.notificationService.getUnreachableNotification("fkdokm", new NotificationRequest()).block();

        Assertions.assertNotNull(notificationsUnreachableResponse);
        Assertions.assertEquals(0L,notificationsUnreachableResponse.getNotificationsCount());

    }

    @Test
    void getUnreachableNotification_No_Operation_Found(){
        Mockito.when(this.dataVaultClient.anonymized(Mockito.any())).thenReturn(Mono.just("taxId"));
        Mockito.when(this.operationDAO.searchOperationsFromRecipientInternalId(Mockito.any())).thenReturn(Flux.empty());
        Mockito.when(this.deliveryPushClient.paperNotificationFailed(Mockito.anyString())).thenReturn(Flux.empty());

        NotificationsUnreachableResponse notificationsUnreachableResponse = this.notificationService.getUnreachableNotification("fkdokm", new NotificationRequest()).block();

        Assertions.assertNotNull(notificationsUnreachableResponse);
        Assertions.assertEquals(0L,notificationsUnreachableResponse.getNotificationsCount());
    }

    @Test
    void getUnreachableNotification_PnDeliveryPush_error(){
        Mockito.when(this.dataVaultClient.anonymized(Mockito.any())).thenReturn(Mono.just("taxId2"));
        Mockito.when(this.operationDAO.searchOperationsFromRecipientInternalId(Mockito.any())).thenReturn(Flux.empty());
        Mockito.when(this.deliveryPushClient.paperNotificationFailed(Mockito.anyString())).thenThrow(new PnGenericException(ERROR_GET_UNREACHABLE_NOTIFICATION, ERROR_GET_UNREACHABLE_NOTIFICATION.getMessage()));

        try {
            this.notificationService.getUnreachableNotification("fkdokm", new NotificationRequest())
                    .block();
            Assertions.fail("Expected an PnGenericException to be thrown");
        } catch (PnGenericException e) {
            Assertions.assertEquals(ExceptionTypeEnum.ERROR_GET_UNREACHABLE_NOTIFICATION, e.getExceptionType());
        }
    }

    private List<PnServiceDeskOperations> getOperations(){

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

    private List<PnServiceDeskOperations> getOperationsNoUnreachable(){

        pnServiceDeskAttachments.setIun("iun123");
        pnServiceDeskAttachments2.setIun("iun124");
        pnServiceDeskAttachments3.setIun("iun122");
        pnServiceDeskAttachments4.setIun("iun125");

        List<PnServiceDeskAttachments> attachments = new ArrayList<>();
        attachments.add(pnServiceDeskAttachments);
        attachments.add(pnServiceDeskAttachments2);
        attachments.add(pnServiceDeskAttachments3);
        attachments.add(pnServiceDeskAttachments4);

        pnServiceDeskOperations.setOperationId("123");
        pnServiceDeskOperations.setStatus("OK");
        pnServiceDeskOperations.setAttachments(attachments);
        pnServiceDeskOperations2.setOperationId("1235");
        pnServiceDeskOperations2.setStatus("OK");
        pnServiceDeskOperations2.setAttachments(attachments);
        pnServiceDeskOperations3.setOperationId("1234");
        pnServiceDeskOperations3.setStatus("OK");
        pnServiceDeskOperations3.setAttachments(attachments);
        pnServiceDeskOperations4.setOperationId("12346");
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
