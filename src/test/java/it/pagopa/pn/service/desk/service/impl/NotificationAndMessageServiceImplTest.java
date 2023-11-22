package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.*;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.*;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.NotificationStatusDto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault.PnDataVaultClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_ON_DELIVERY_CLIENT;
import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_ON_DELIVERY_PUSH_CLIENT;

class NotificationAndMessageServiceImplTest extends BaseTest {

    @MockBean
    private PnDataVaultClient dataVaultClient;
    @MockBean
    private PnDeliveryClient pnDeliveryClient;
    @MockBean
    private PnDeliveryPushClient pnDeliveryPushClient;
    @Autowired
    private NotificationAndMessageServiceImpl notificationAndMessageService;

    @Test
    void searchNotificationsFromTaxId(){
        Mockito.when(this.dataVaultClient.anonymized(Mockito.any(), Mockito.any())).thenReturn(Mono.just("taxId"));
        Mockito.when(this.pnDeliveryPushClient.getNotificationHistory(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(getNotificationHistoryResponseDto()));
        Mockito.when(this.pnDeliveryClient.searchNotificationsPrivate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(getNotificationSearchResponseDto()));
        SearchNotificationsResponse actualResponse = notificationAndMessageService.searchNotificationsFromTaxId("fkdokm",
                OffsetDateTime.parse("2023-08-15T15:49:05.63Z"),
                OffsetDateTime.parse("2023-08-31T15:49:05.63Z"),
                50,
                "nextPageKey",
                getSearchMessageRequest("taxId", RecipientType.PF)).block();

        Assertions.assertNotNull(actualResponse);
        Assertions.assertEquals(true, actualResponse.getMoreResult());
        Assertions.assertEquals("nextPageKey", actualResponse.getNextPagesKey().get(0));
        Assertions.assertEquals(IunStatus.ACCEPTED, actualResponse.getResults().get(0).getIunStatus());
    }

    @Test
    void searchNotificationsFromTaxIdWhenDataVaultClientEmpty(){
        Mockito.when(this.dataVaultClient.anonymized(Mockito.any(), Mockito.any())).thenReturn(Mono.empty());
        SearchNotificationsResponse actualResponse = notificationAndMessageService.searchNotificationsFromTaxId(null,null,null,
                null,null,getSearchMessageRequest(null, RecipientType.PF)).block();

        Assertions.assertNull(actualResponse.getMoreResult());
        Assertions.assertNull(actualResponse.getNextPagesKey());
        Assertions.assertEquals(0, actualResponse.getResults().size());
    }

    @Test
    void searchNotificationsFromTaxIdWhenPnDeliveryClientError(){
        PnGenericException pnGenericException = new PnGenericException(ERROR_ON_DELIVERY_CLIENT, ERROR_ON_DELIVERY_CLIENT.getMessage());
        Mockito.when(this.dataVaultClient.anonymized(Mockito.any(), Mockito.any())).thenReturn(Mono.just("taxId"));
        Mockito.when(this.pnDeliveryClient.searchNotificationsPrivate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.error(pnGenericException));

        StepVerifier.create(notificationAndMessageService.searchNotificationsFromTaxId("fkdokm",
                        OffsetDateTime.parse("2023-08-15T15:49:05.63Z"),
                        OffsetDateTime.parse("2023-08-31T15:49:05.63Z"),
                        50,
                        "nextPageKey",
                        getSearchMessageRequest("taxId", RecipientType.PF)))
                .expectError(PnGenericException.class)
                .verify();
    }

    @Test
    void searchNotificationsFromTaxIdWhenPnDeliveryPushClientError(){
        PnGenericException pnGenericException = new PnGenericException(ERROR_ON_DELIVERY_PUSH_CLIENT, ERROR_ON_DELIVERY_PUSH_CLIENT.getMessage());
        Mockito.when(this.dataVaultClient.anonymized(Mockito.any(), Mockito.any())).thenReturn(Mono.just("taxId"));
        Mockito.when(this.pnDeliveryPushClient.getNotificationHistory(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.error(pnGenericException));
        Mockito.when(this.pnDeliveryClient.searchNotificationsPrivate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(getNotificationSearchResponseDto()));

        StepVerifier.create(notificationAndMessageService.searchNotificationsFromTaxId("fkdokm",
                        OffsetDateTime.parse("2023-08-15T15:49:05.63Z"),
                        OffsetDateTime.parse("2023-08-31T15:49:05.63Z"),
                        50,
                        "nextPageKey",
                        getSearchMessageRequest("taxId", RecipientType.PF)))
                .expectError(PnGenericException.class)
                .verify();
    }

    @Test
    void getTimelineOfIUNTest(){
        Mockito.when(this.pnDeliveryClient.getSentNotificationPrivate(Mockito.any())).thenReturn(Mono.just(getSentNotificationV21Dto()));
        Mockito.when(this.pnDeliveryPushClient.getNotificationHistory(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(getHistory()));

        TimelineResponse response = notificationAndMessageService.getTimelineOfIUN("test", "PRVZ-NZKM-JEDK-202309-A-1").block();
        Assertions.assertNotNull(response);

    }

    @Test
    void getTimelineOfIUNPnDeliveryClientError(){
        PnGenericException pnGenericException = new PnGenericException(ERROR_ON_DELIVERY_CLIENT, ERROR_ON_DELIVERY_CLIENT.getMessage());
        Mockito.when(this.pnDeliveryClient.getSentNotificationPrivate(Mockito.any())).thenReturn(Mono.error(pnGenericException));

        StepVerifier.create(notificationAndMessageService.getTimelineOfIUN("test", "PRVZ-NZKM-JEDK-202309-A-1"))
                .expectError(PnGenericException.class)
                .verify();
    }

    @Test
    void getTimelineOfIUNPnDeliveryPushClientError(){
        PnGenericException pnGenericException = new PnGenericException(ERROR_ON_DELIVERY_PUSH_CLIENT, ERROR_ON_DELIVERY_PUSH_CLIENT.getMessage());
        Mockito.when(this.pnDeliveryClient.getSentNotificationPrivate(Mockito.any())).thenReturn(Mono.just(getSentNotificationV21Dto()));
        Mockito.when(this.pnDeliveryPushClient.getNotificationHistory(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.error(pnGenericException));

        StepVerifier.create(notificationAndMessageService.getTimelineOfIUN("test", "PRVZ-NZKM-JEDK-202309-A-1"))
                .expectError(PnGenericException.class)
                .verify();
    }

    @Test
    void getDocumentsOfIunTest (){
        Mockito.when(this.dataVaultClient.anonymized(Mockito.any(), Mockito.any())).thenReturn(Mono.just("taxId"));
        Mockito.when(this.pnDeliveryClient.getSentNotificationPrivate(Mockito.any())).thenReturn(Mono.just(getSentNotificationV21Dto()));
        Mockito.when(this.pnDeliveryPushClient.getNotificationHistory(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(getHistory()));
        Mockito.when(this.pnDeliveryClient.getReceivedNotificationDocumentPrivate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(getDocuments()));
        DocumentsRequest request = new DocumentsRequest();
        request.setRecipientType(RecipientType.PF);
        request.setTaxId("FRMTTR76M06B715E");
        DocumentsResponse response = notificationAndMessageService.getDocumentsOfIun("PRVZ-NZKM-JEDK-202309-A-1", request).block();
        Assertions.assertNotNull(response);

    }

    @Test
    void getDocumentsOfIunNotificationCancelledTest (){
        Mockito.when(this.dataVaultClient.anonymized(Mockito.any(), Mockito.any())).thenReturn(Mono.just("taxId"));
        Mockito.when(this.pnDeliveryClient.getSentNotificationPrivate(Mockito.any())).thenReturn(Mono.just(getSentNotificationV21Dto()));
        Mockito.when(this.pnDeliveryPushClient.getNotificationHistory(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(getHistoryNotificationCancellation()));
        DocumentsRequest request = new DocumentsRequest();
        request.setRecipientType(RecipientType.PF);
        request.setTaxId("FRMTTR76M06B715E");
        DocumentsResponse response = notificationAndMessageService.getDocumentsOfIun("PRVZ-NZKM-JEDK-202309-A-1", request).block();
        Assertions.assertNotNull(response);

    }

    @Test
    void getDocumentsOfIunPnDeliveryClientError(){
        PnGenericException pnGenericException = new PnGenericException(ERROR_ON_DELIVERY_CLIENT, ERROR_ON_DELIVERY_CLIENT.getMessage());
        Mockito.when(this.dataVaultClient.anonymized(Mockito.any(), Mockito.any())).thenReturn(Mono.just("taxId"));
        Mockito.when(this.pnDeliveryClient.getSentNotificationPrivate(Mockito.any())).thenReturn(Mono.error(pnGenericException));
        DocumentsRequest request = new DocumentsRequest();
        request.setRecipientType(RecipientType.PF);
        request.setTaxId("FRMTTR76M06B715E");

        StepVerifier.create(notificationAndMessageService.getDocumentsOfIun("PRVZ-NZKM-JEDK-202309-A-1", request))
                .expectError(PnGenericException.class)
                .verify();
    }

    @Test
    void getNotificationFromIUNTest (){
        Mockito.when(this.pnDeliveryClient.getSentNotificationPrivate(Mockito.any())).thenReturn(Mono.just(getSentNotificationV21Dto()));
        NotificationDetailResponse response = notificationAndMessageService.getNotificationFromIUN("PRVZ-NZKM-JEDK-202309-A-1").block();
        Assertions.assertNotNull(response);
    }

    @Test
    void getNotificationFromIUNPnDeliveryClientError(){
        PnGenericException pnGenericException = new PnGenericException(ERROR_ON_DELIVERY_CLIENT, ERROR_ON_DELIVERY_CLIENT.getMessage());
        Mockito.when(this.pnDeliveryClient.getSentNotificationPrivate(Mockito.any())).thenReturn(Mono.error(pnGenericException));

        StepVerifier.create(notificationAndMessageService.getNotificationFromIUN("PRVZ-NZKM-JEDK-202309-A-1"))
                .expectError(PnGenericException.class)
                .verify();
    }

    @Test
    void searchNotificationsAsDelegateFromInternalIdTest(){
        Mockito.when(pnDeliveryClient.searchNotificationsPrivate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(getNotificationSearchResponseDto()));
        Mockito.when(this.pnDeliveryPushClient.getNotificationHistory(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(getNotificationHistoryResponseDto()));

        SearchNotificationsResponse response = notificationAndMessageService.searchNotificationsAsDelegateFromInternalId("", "", "", 1, "", OffsetDateTime.now(), OffsetDateTime.now()).block();
        Assertions.assertNotNull(response);
    }

    @Test
    void searchNotificationsAsDelegateFromInternalIdDeliveryClientError(){
        PnGenericException pnGenericException = new PnGenericException(ERROR_ON_DELIVERY_CLIENT, ERROR_ON_DELIVERY_CLIENT.getMessage());
        Mockito.when(pnDeliveryClient.searchNotificationsPrivate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.error(pnGenericException));


        StepVerifier.create(notificationAndMessageService.searchNotificationsAsDelegateFromInternalId("", "", "", 1, "", OffsetDateTime.now(), OffsetDateTime.now()))
                .expectError(PnGenericException.class)
                .verify();
    }

    @Test
    void searchNotificationsAsDelegateFromInternalIdDeliveryPushClientError(){
        PnGenericException pnGenericException = new PnGenericException(ERROR_ON_DELIVERY_PUSH_CLIENT, ERROR_ON_DELIVERY_PUSH_CLIENT.getMessage());
        Mockito.when(pnDeliveryClient.searchNotificationsPrivate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(getNotificationSearchResponseDto()));
        Mockito.when(this.pnDeliveryPushClient.getNotificationHistory(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.error(pnGenericException));


        StepVerifier.create(notificationAndMessageService.searchNotificationsAsDelegateFromInternalId("", "", "", 1, "", OffsetDateTime.now(), OffsetDateTime.now()))
                .expectError(PnGenericException.class)
                .verify();
    }

    private SearchNotificationsRequest getSearchMessageRequest(String taxId, RecipientType recipientType) {
        SearchNotificationsRequest searchMessagesRequest = new SearchNotificationsRequest();
        searchMessagesRequest.setTaxId(taxId);
        searchMessagesRequest.setRecipientType(recipientType);
        return searchMessagesRequest;
    }

    private NotificationSearchResponseDto getNotificationSearchResponseDto() {
        NotificationSearchResponseDto notificationSearchResponseDto = new NotificationSearchResponseDto();
        notificationSearchResponseDto.setMoreResult(true);

        List<String> nextPageKeyList = new ArrayList<>();
        nextPageKeyList.add("nextPageKey");
        notificationSearchResponseDto.setNextPagesKey(nextPageKeyList);

        List<NotificationSearchRowDto> notificationSearchRowDtoList = new ArrayList<>();
        NotificationSearchRowDto notificationSearchRowDto = new NotificationSearchRowDto();
        notificationSearchRowDto.setNotificationStatus(it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto
                .NotificationStatusDto.ACCEPTED);
        notificationSearchRowDto.setIun("iun123");
        notificationSearchRowDto.setGroup("group");
        notificationSearchRowDto.setSender("sender");
        notificationSearchRowDto.setSubject("subject");
        notificationSearchRowDto.setSentAt(OffsetDateTime.of(LocalDateTime.now(),
                ZoneOffset.of("+07:00")));
        notificationSearchRowDto.setMandateId("4321");
        notificationSearchRowDto.setPaProtocolNumber("1234");

        List<String> recipients = new ArrayList<>();
        recipients.add("456");

        notificationSearchRowDto.setRecipients(recipients);
        notificationSearchRowDto.setRequestAcceptedAt(OffsetDateTime.of(LocalDateTime.now(),
                ZoneOffset.of("+08:00")));
        notificationSearchRowDtoList.add(notificationSearchRowDto);

        notificationSearchResponseDto.setResultsPage(notificationSearchRowDtoList);

        return notificationSearchResponseDto;
    }

    private NotificationHistoryResponseDto getNotificationHistoryResponseDto() {
        NotificationHistoryResponseDto notificationHistoryResponseDto = new NotificationHistoryResponseDto();
        notificationHistoryResponseDto.setNotificationStatus(NotificationStatusDto.ACCEPTED);

        List<TimelineElementV20Dto> timelineElementDtoList = new ArrayList<>();
        TimelineElementV20Dto timelineElementDto = new TimelineElementV20Dto();
        timelineElementDto.setCategory(TimelineElementCategoryV20Dto.SEND_COURTESY_MESSAGE);
        timelineElementDto.setElementId("elementId");
        timelineElementDto.setDetails(new TimelineElementDetailsV20Dto());
        timelineElementDto.setTimestamp(OffsetDateTime.of(LocalDateTime.now(),
                ZoneOffset.of("+07:00")));

        List<LegalFactListElementDto> legalFactListElementDtoList = new ArrayList<>();
        LegalFactListElementDto legalFactListElementDto = new LegalFactListElementDto();
        legalFactListElementDto.setLegalFactsId(new LegalFactsIdDto());
        legalFactListElementDto.setTaxId("taxId");
        legalFactListElementDto.setIun("iun123");
        legalFactListElementDtoList.add(legalFactListElementDto);

        notificationHistoryResponseDto.setTimeline(timelineElementDtoList);

        List<NotificationStatusHistoryElementDto> notificationStatusHistoryElementDtoList = new ArrayList<>();
        NotificationStatusHistoryElementDto notificationStatusHistoryElementDto = new NotificationStatusHistoryElementDto();
        notificationStatusHistoryElementDto.setStatus(NotificationStatusDto.ACCEPTED);
        notificationStatusHistoryElementDto.setActiveFrom(OffsetDateTime.of(LocalDateTime.now(),
                ZoneOffset.of("+07:00")));

        List<String> relativeTimeLineElements = new ArrayList<>();
        relativeTimeLineElements.add("relativeTimeLineElement");
        notificationStatusHistoryElementDto.setRelatedTimelineElements(relativeTimeLineElements);
        notificationStatusHistoryElementDtoList.add(notificationStatusHistoryElementDto);

        notificationHistoryResponseDto.setNotificationStatusHistory(notificationStatusHistoryElementDtoList);

        return notificationHistoryResponseDto;
    }

    private SentNotificationV21Dto getSentNotificationV21Dto (){
        SentNotificationV21Dto sentNotificationV21Dto = new SentNotificationV21Dto();
        sentNotificationV21Dto.setSentAt(OffsetDateTime.now());
        NotificationRecipientV21Dto notificationRecipientV21Dto = new NotificationRecipientV21Dto();
        notificationRecipientV21Dto.setRecipientType(NotificationRecipientV21Dto.RecipientTypeEnum.PF);
        notificationRecipientV21Dto.setPayments(new ArrayList<>());
        sentNotificationV21Dto.setPhysicalCommunicationType(SentNotificationV21Dto.PhysicalCommunicationTypeEnum.AR_REGISTERED_LETTER);
        sentNotificationV21Dto.setSenderDenomination("comune");
        sentNotificationV21Dto.setSenderTaxId("FRMTTR76M06B715E");
        sentNotificationV21Dto.setSentAt(OffsetDateTime.now());
        sentNotificationV21Dto.setPaymentExpirationDate("31/12/2023");
        List<NotificationRecipientV21Dto> dtoList = new ArrayList<>();
        dtoList.add(notificationRecipientV21Dto);
        sentNotificationV21Dto.setRecipients(dtoList);
        return sentNotificationV21Dto;
    }

    private NotificationHistoryResponseDto getHistory (){
        NotificationHistoryResponseDto historyResponseDto = new NotificationHistoryResponseDto();
        historyResponseDto.setNotificationStatus(NotificationStatusDto.ACCEPTED);
        TimelineElementV20Dto timelineElementV20Dto = new TimelineElementV20Dto();
        timelineElementV20Dto.setCategory(TimelineElementCategoryV20Dto.REQUEST_ACCEPTED);
        timelineElementV20Dto.setDetails(new TimelineElementDetailsV20Dto());
        timelineElementV20Dto.setTimestamp(OffsetDateTime.now());
        List<TimelineElementV20Dto> dtoList = new ArrayList<>();
        dtoList.add(timelineElementV20Dto);
        historyResponseDto.setTimeline(dtoList);
        return historyResponseDto;
    }

    private NotificationHistoryResponseDto getHistoryNotificationCancellation (){
        NotificationHistoryResponseDto historyResponseDto = new NotificationHistoryResponseDto();
        historyResponseDto.setNotificationStatus(NotificationStatusDto.ACCEPTED);
        TimelineElementV20Dto timelineElementV20Dto = new TimelineElementV20Dto();
        timelineElementV20Dto.setCategory(TimelineElementCategoryV20Dto.NOTIFICATION_CANCELLATION_REQUEST);
        timelineElementV20Dto.setDetails(new TimelineElementDetailsV20Dto());
        timelineElementV20Dto.setTimestamp(OffsetDateTime.now());
        List<TimelineElementV20Dto> dtoList = new ArrayList<>();
        dtoList.add(timelineElementV20Dto);
        historyResponseDto.setTimeline(dtoList);
        return historyResponseDto;
    }

    private NotificationAttachmentDownloadMetadataResponseDto getDocuments (){
        NotificationAttachmentDownloadMetadataResponseDto responseDto = new NotificationAttachmentDownloadMetadataResponseDto();
        responseDto.setContentLength(1234);
        responseDto.setContentType("pdf");
        responseDto.setFilename("file_test");
        return responseDto;
    }

}
