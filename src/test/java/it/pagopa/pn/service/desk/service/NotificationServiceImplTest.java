package it.pagopa.pn.service.desk.service;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.ResponsePaperNotificationFailedDtoDto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationsUnreachableResponse;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
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

import java.util.ArrayList;
import java.util.List;

public class NotificationServiceImplTest extends BaseTest.WithMockServer {

    @Autowired
    private NotificationService notificationService;

    @MockBean
    private PnDataVaultClient dataVaultClient;
    @MockBean
    private OperationDAO operationDAO;


    List<ResponsePaperNotificationFailedDtoDto> listNotification = new ArrayList<>();
    ResponsePaperNotificationFailedDtoDto responsePaperNotificationFailedDtoDto = new ResponsePaperNotificationFailedDtoDto();
    PnServiceDeskOperations pnServiceDeskOperations = new PnServiceDeskOperations();
    PnServiceDeskOperations pnServiceDeskOperations2 = new PnServiceDeskOperations();
    PnServiceDeskOperations pnServiceDeskOperations3 = new PnServiceDeskOperations();
    PnServiceDeskOperations pnServiceDeskOperations4 = new PnServiceDeskOperations();



    @Test
    void getUnreachableNotification(){

        Mockito.when(this.dataVaultClient.anonymized(Mockito.any())).thenReturn(Mono.just("taxId"));
        Mockito.when(this.operationDAO.searchOperationsFromRecipientInternalId(Mockito.any())).thenReturn(Flux.fromIterable(getOperations()));

        NotificationsUnreachableResponse notificationsUnreachableResponse = this.notificationService.getUnreachableNotification("fkdokm", new NotificationRequest()).block();

        Assertions.assertNotNull(notificationsUnreachableResponse);
        Assertions.assertEquals(3L,notificationsUnreachableResponse.getNotificationsCount());

    }

    @Test
    void getUnreachableNotification_No_Notification_Unreachable(){
        Mockito.when(this.dataVaultClient.anonymized(Mockito.any())).thenReturn(Mono.just("taxId"));
        Mockito.when(this.operationDAO.searchOperationsFromRecipientInternalId(Mockito.any())).thenReturn(Flux.fromIterable(getOperationsNoUnreachable()));

        NotificationsUnreachableResponse notificationsUnreachableResponse = this.notificationService.getUnreachableNotification("fkdokm", new NotificationRequest()).block();

        Assertions.assertNotNull(notificationsUnreachableResponse);
        Assertions.assertEquals(0L,notificationsUnreachableResponse.getNotificationsCount());

    }

    private List<PnServiceDeskOperations> getOperations(){

        pnServiceDeskOperations.setOperationId("123");
        pnServiceDeskOperations.setStatus("PREPARING");
        pnServiceDeskOperations2.setOperationId("1235");
        pnServiceDeskOperations2.setStatus("PREPARING");
        pnServiceDeskOperations3.setOperationId("1234");
        pnServiceDeskOperations3.setStatus("PREPARING");
        pnServiceDeskOperations3.setOperationId("1234");
        pnServiceDeskOperations3.setStatus("OK");

        List<PnServiceDeskOperations> operations = new ArrayList<>();

        operations.add(pnServiceDeskOperations);
        operations.add(pnServiceDeskOperations2);
        operations.add(pnServiceDeskOperations3);
        operations.add(pnServiceDeskOperations4);

        return operations;
    }

    private List<PnServiceDeskOperations> getOperationsNoUnreachable(){

        pnServiceDeskOperations.setOperationId("123");
        pnServiceDeskOperations.setStatus("OK");
        pnServiceDeskOperations2.setOperationId("1235");
        pnServiceDeskOperations2.setStatus("OK");
        pnServiceDeskOperations3.setOperationId("1234");
        pnServiceDeskOperations3.setStatus("OK");
        pnServiceDeskOperations4.setOperationId("1234");
        pnServiceDeskOperations4.setStatus("OK");

        List<PnServiceDeskOperations> operations = new ArrayList<>();

        operations.add(pnServiceDeskOperations);
        operations.add(pnServiceDeskOperations2);
        operations.add(pnServiceDeskOperations3);
        operations.add(pnServiceDeskOperations4);

        return operations;
    }
}
