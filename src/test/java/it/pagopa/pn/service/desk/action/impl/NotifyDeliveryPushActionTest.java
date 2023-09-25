package it.pagopa.pn.service.desk.action.impl;

import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.exception.ExceptionTypeEnum;
import it.pagopa.pn.service.desk.exception.PnEntityNotFoundException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.ResponseNotificationViewedDtoDto;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClient;
import it.pagopa.pn.service.desk.middleware.queue.model.InternalEvent;
import it.pagopa.pn.service.desk.middleware.queue.model.InternalEventBody;
import it.pagopa.pn.service.desk.middleware.queue.producer.InternalQueueMomProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NotifyDeliveryPushActionTest {

    @InjectMocks
    private NotifyDeliveryPushActionImpl notifyDeliveryPushAction;
    @Mock
    private PnDeliveryPushClient pnDeliveryPushClient;
    @Mock
    private OperationDAO operationDAO;
    @Mock
    private InternalQueueMomProducer internalQueueMomProducer;

    @Mock
    private PnServiceDeskConfigs configs;

    @Test
    void executeCaseExceptionEntityNotFound() {
        InternalEventBody internalEventBody = getInternalEventBodyNullIuns();
        Mockito.when(operationDAO.getByOperationId(Mockito.any()))
                .thenReturn(Mono.empty());
        PnEntityNotFoundException exception = assertThrows(PnEntityNotFoundException.class, () -> notifyDeliveryPushAction.execute(internalEventBody));
        assertEquals(exception.getExceptionType(), ExceptionTypeEnum.ENTITY_NOT_FOUND);
    }

    @Test
    void executeCaseIunsNullIsNotifiedAttachmentsFalse(){
        InternalEventBody internalEventBody = getInternalEventBodyNullIuns();

        PnServiceDeskOperations entity = new PnServiceDeskOperations();
        entity.setAttachments(getAttachments(false));

        Mockito.when(operationDAO.getByOperationId("QWERTY"))
                .thenReturn(Mono.just(entity));

        Mockito.when(operationDAO.updateEntity(entity))
                .thenReturn(Mono.just(entity));

        assertDoesNotThrow(() -> notifyDeliveryPushAction.execute(internalEventBody));

    }

    @Test
    void executeCaseIunsNullIsNotifiedAttachmentsTrue(){
        InternalEventBody internalEventBody = getInternalEventBodyNullIuns();

        PnServiceDeskOperations entity = new PnServiceDeskOperations();
        entity.setAttachments(getAttachments(true));

        Mockito.when(operationDAO.getByOperationId("QWERTY"))
                .thenReturn(Mono.just(entity));

        Mockito.when(operationDAO.updateEntity(entity))
                .thenReturn(Mono.just(entity));

        assertDoesNotThrow(() -> notifyDeliveryPushAction.execute(internalEventBody));

    }

    @Test
    void executeCaseNotifyNotificationViewedEmpty(){
        InternalEventBody internalEventBody = getInternalEventBody();

        PnServiceDeskOperations entity = new PnServiceDeskOperations();
        entity.setAttachments(getAttachments(true));

        Mockito.when(operationDAO.getByOperationId("QWERTY"))
                .thenReturn(Mono.just(entity));

        Mockito.when(operationDAO.updateEntity(entity))
                .thenReturn(Mono.just(entity));

        Mockito.when(this.pnDeliveryPushClient.notifyNotificationViewed(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Mono.empty());

        Mockito.when(configs.getNotifyAttempt()).thenReturn(2);

        assertDoesNotThrow(() -> notifyDeliveryPushAction.execute(internalEventBody));

    }

    @Test
    void executeCaseNotifyNotificationViewedEmptyAndOperationEmpty(){
        InternalEventBody internalEventBody = getInternalEventBody();

        PnServiceDeskOperations entity = new PnServiceDeskOperations();
        entity.setAttachments(getAttachments(true));

        Mockito.when(operationDAO.getByOperationId(Mockito.any()))
                .thenReturn(Mono.empty());

        Mockito.when(this.pnDeliveryPushClient.notifyNotificationViewed(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Mono.empty());

        Mockito.when(configs.getNotifyAttempt()).thenReturn(2);

        Mockito.doNothing().when(internalQueueMomProducer)
                .push((InternalEvent) Mockito.any());

        assertDoesNotThrow(() -> notifyDeliveryPushAction.execute(internalEventBody));

    }

    @Test
    void executeCaseNotifyNotificationViewedEmptyAndOperationEmptyTryAnotherAttempt(){
        InternalEventBody internalEventBody = getInternalEventBodyNullIuns();
        List<String> iuns = new ArrayList<>();
        String iun = "LJLH-GNTJ-DVXR-202209-J-1";
        iuns.add(iun);
        internalEventBody.setIuns(iuns);

        PnServiceDeskOperations entity = new PnServiceDeskOperations();
        entity.setAttachments(getAttachments(true));

        Mockito.when(this.pnDeliveryPushClient.notifyNotificationViewed(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Mono.empty());

        Mockito.when(configs.getNotifyAttempt()).thenReturn(2);

        Mockito.doNothing().when(internalQueueMomProducer)
                .push((InternalEvent) Mockito.any());

        assertDoesNotThrow(() -> notifyDeliveryPushAction.execute(internalEventBody));

    }

    @Test
    void executeCaseNotifyNotificationViewed(){
        InternalEventBody internalEventBody = getInternalEventBody();

        PnServiceDeskOperations entity = new PnServiceDeskOperations();
        entity.setAttachments(getAttachments(true));

        Mockito.when(operationDAO.getByOperationId("QWERTY"))
                .thenReturn(Mono.just(entity));

        Mockito.when(operationDAO.updateEntity(entity))
                .thenReturn(Mono.just(entity));

        Mockito.when(this.pnDeliveryPushClient.notifyNotificationViewed(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Mono.just(getNotificationViewed()));

        Mockito.when(configs.getNotifyAttempt()).thenReturn(2);

        Mockito.doNothing().when(internalQueueMomProducer)
                .push((InternalEvent) Mockito.any());

        assertDoesNotThrow(() -> notifyDeliveryPushAction.execute(internalEventBody));

    }

    private InternalEventBody getInternalEventBodyNullIuns(){
        InternalEventBody internalEventBody = new InternalEventBody();
        internalEventBody.setOperationId("QWERTY");
        internalEventBody.setRecipientInternalId("PF-4fc75df3-0913-407e-bdaa-e50329708b7d");
        internalEventBody.setIuns(null);
        internalEventBody.setAttempt(1);
        return internalEventBody;
    }

    private InternalEventBody getInternalEventBody(){
        InternalEventBody internalEventBody = new InternalEventBody();
        internalEventBody.setOperationId("QWERTY");
        internalEventBody.setRecipientInternalId("PF-4fc75df3-0913-407e-bdaa-e50329708b7d");
        List<String> iuns = new ArrayList<>();
        String iun = "LJLH-GNTJ-DVXR-202209-J-1";
        iuns.add(iun);
        internalEventBody.setIuns(iuns);
        internalEventBody.setAttempt(2);
        return internalEventBody;
    }

    private List<PnServiceDeskAttachments> getAttachments (boolean isNotified){
        List<PnServiceDeskAttachments> attachments = new ArrayList<>();
        PnServiceDeskAttachments attachment = new PnServiceDeskAttachments();
        attachment.setIun("LJLH-GNTJ-DVXR-202209-J-1");
        attachment.setIsAvailable(true);
        attachment.setIsNotified(isNotified);
        List<String> fileKeys = new ArrayList<>();
        String fileKey = "safestorage://981234";
        fileKeys.add(fileKey);
        attachment.setFilesKey(fileKeys);
        attachments.add(attachment);
        return attachments;
    }

    private ResponseNotificationViewedDtoDto getNotificationViewed(){
        ResponseNotificationViewedDtoDto notificationViewedDto = new ResponseNotificationViewedDtoDto();
        notificationViewedDto.setIun("LJLH-GNTJ-DVXR-202209-J-1");
        return notificationViewedDto;
    }
}
