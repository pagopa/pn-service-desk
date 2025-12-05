package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.*;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.*;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.NotificationStatusV26Dto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.payment.v1.dto.DetailDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.payment.v1.dto.PaymentInfoRequestDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.payment.v1.dto.PaymentInfoV21Dto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.payment.v1.dto.PaymentStatusDto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault.PnDataVaultClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.externalregistries.ExternalRegistriesClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_ON_DELIVERY_CLIENT;
import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_ON_DELIVERY_PUSH_CLIENT;

@ExtendWith(MockitoExtension.class)
class NotificationAndMessageServiceImplTest  {
    @Mock
    private PnDataVaultClient dataVaultClient;
    @Mock
    private PnDeliveryClient pnDeliveryClient;
    @Mock
    private PnDeliveryPushClient pnDeliveryPushClient;
    @Mock
    private ExternalRegistriesClient externalRegistriesClient;

    @Spy
    private AuditLogServiceImpl auditLogService;
    @InjectMocks
    private NotificationAndMessageServiceImpl notificationAndMessageService;


    @Test
    void searchNotificationsFromTaxId() {
        Mockito.when(this.dataVaultClient.anonymized(Mockito.any(), Mockito.any())).thenReturn(Mono.just("PF-4fc75df3-0913-407e-bdaa-e50329708b7d"));
        Mockito.when(this.pnDeliveryPushClient.getNotificationHistory(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.just(getNotificationHistoryResponseDto()));
        Mockito.when(this.pnDeliveryClient.searchNotificationsPrivate(Instant.parse("2023-08-15T15:49:05.630Z"),
                        Instant.parse("2023-08-31T15:49:05.630Z"), "PF-4fc75df3-0913-407e-bdaa-e50329708b7d",
                        null, null, null, 50, "nextPageKey"))
                .thenReturn(Mono.just(getNotificationSearchResponseDto()));
        SearchNotificationsResponse actualResponse = notificationAndMessageService.searchNotificationsFromTaxId("fkdokm",
                Instant.parse("2023-08-15T15:49:05.63Z"),
                Instant.parse("2023-08-31T15:49:05.63Z"),
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

        Assertions.assertNotNull(actualResponse);
        Assertions.assertNull(actualResponse.getMoreResult());
        Assertions.assertTrue(actualResponse.getNextPagesKey().isEmpty());
        Assertions.assertEquals(0, actualResponse.getResults().size());
    }

    @Test
    void searchNotificationsFromTaxIdWhenPnDeliveryClientError(){
        PnGenericException pnGenericException = new PnGenericException(ERROR_ON_DELIVERY_CLIENT, ERROR_ON_DELIVERY_CLIENT.getMessage());
        Mockito.when(this.dataVaultClient.anonymized(Mockito.any(), Mockito.any())).thenReturn(Mono.just("PF-4fc75df3-0913-407e-bdaa-e50329708b7d"));
        Mockito.when(this.pnDeliveryClient.searchNotificationsPrivate(Instant.parse("2023-08-15T15:49:05.630Z"),
                        Instant.parse("2023-08-31T15:49:05.630Z"), "PF-4fc75df3-0913-407e-bdaa-e50329708b7d",
                        null, null, null, 50, "nextPageKey"))
                .thenReturn(Mono.error(pnGenericException));

        StepVerifier.create(notificationAndMessageService.searchNotificationsFromTaxId("fkdokm",
                        Instant.parse("2023-08-15T15:49:05.63Z"),
                        Instant.parse("2023-08-31T15:49:05.63Z"),
                        50,
                        "nextPageKey",
                        getSearchMessageRequest("taxId", RecipientType.PF)))
                .expectError(PnGenericException.class)
                .verify();
    }

    @Test
    void searchNotificationsFromTaxIdWhenPnDeliveryPushClientError(){
        PnGenericException pnGenericException = new PnGenericException(ERROR_ON_DELIVERY_PUSH_CLIENT, ERROR_ON_DELIVERY_PUSH_CLIENT.getMessage());
        Mockito.when(this.dataVaultClient.anonymized(Mockito.any(), Mockito.any())).thenReturn(Mono.just("PF-4fc75df3-0913-407e-bdaa-e50329708b7d"));
        Mockito.when(this.pnDeliveryPushClient.getNotificationHistory(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Mono.error(pnGenericException));
        Mockito.when(this.pnDeliveryClient.searchNotificationsPrivate(Instant.parse("2023-08-15T15:49:05.630Z"),
                        Instant.parse("2023-08-31T15:49:05.630Z"), "PF-4fc75df3-0913-407e-bdaa-e50329708b7d",
                        null, null, null, 50, "nextPageKey"))
                .thenReturn(Mono.just(getNotificationSearchResponseDto()));

        StepVerifier.create(notificationAndMessageService.searchNotificationsFromTaxId("fkdokm",
                        Instant.parse("2023-08-15T15:49:05.63Z"),
                        Instant.parse("2023-08-31T15:49:05.63Z"),
                        50,
                        "nextPageKey",
                        getSearchMessageRequest("taxId", RecipientType.PF)))
                .expectError(PnGenericException.class)
                .verify();
    }

    @Test
    void getTimelineOfIUNTest(){
        Mockito.when(this.pnDeliveryClient.getSentNotificationPrivate(Mockito.any())).thenReturn(Mono.just(getSentNotificationV25Dto()));
        Mockito.when(this.pnDeliveryPushClient.getNotificationHistory(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(getHistory()));

        TimelineResponse response = notificationAndMessageService.getTimelineOfIUN("test", "PRVZ-NZKM-JEDK-202309-A-1", null).block();
        Assertions.assertNotNull(response);
        Assertions.assertEquals(IunStatus.ACCEPTED, response.getIunStatus());
        Assertions.assertEquals(1, response.getTimeline().size());

    }

    @Test
    void getTimelineOfIUNPnDeliveryClientError(){
        PnGenericException pnGenericException = new PnGenericException(ERROR_ON_DELIVERY_CLIENT, ERROR_ON_DELIVERY_CLIENT.getMessage());
        Mockito.when(this.pnDeliveryClient.getSentNotificationPrivate(Mockito.any())).thenReturn(Mono.error(pnGenericException));


        StepVerifier.create(notificationAndMessageService.getTimelineOfIUN("test", "PRVZ-NZKM-JEDK-202309-A-1", null))
                .expectError(PnGenericException.class)
                .verify();
    }

    @Test
    void getTimelineOfIUNPnDeliveryPushClientError(){
        PnGenericException pnGenericException = new PnGenericException(ERROR_ON_DELIVERY_PUSH_CLIENT, ERROR_ON_DELIVERY_PUSH_CLIENT.getMessage());
        Mockito.when(this.pnDeliveryClient.getSentNotificationPrivate(Mockito.any())).thenReturn(Mono.just(getSentNotificationV25Dto()));
        Mockito.when(this.pnDeliveryPushClient.getNotificationHistory(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.error(pnGenericException));

        StepVerifier.create(notificationAndMessageService.getTimelineOfIUN("test", "PRVZ-NZKM-JEDK-202309-A-1", null))
                .expectError(PnGenericException.class)
                .verify();
    }

    @Test
    void getDocumentsOfIunTest (){
        Mockito.when(this.dataVaultClient.anonymized(Mockito.any(), Mockito.any())).thenReturn(Mono.just("PF-4fc75df3-0913-407e-bdaa-e50329708b7d"));
        Mockito.when(this.pnDeliveryClient.getSentNotificationPrivate(Mockito.any())).thenReturn(Mono.just(getSentNotificationV25Dto()));
        Mockito.when(this.pnDeliveryPushClient.getNotificationHistory(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Mono.just(getHistory()));
        DocumentsRequest request = new DocumentsRequest();
        request.setRecipientType(RecipientType.PF);
        request.setTaxId("FRMTTR76M06B715E");
        DocumentsResponse response = notificationAndMessageService.getDocumentsOfIun("PRVZ-NZKM-JEDK-202309-A-1", request).block();
        Assertions.assertNotNull(response);

    }

    @Test
    void getDocumentsOfIunNotificationCancelledTest (){
        Mockito.when(this.dataVaultClient.anonymized(Mockito.any(), Mockito.any())).thenReturn(Mono.just("PF-4fc75df3-0913-407e-bdaa-e50329708b7d"));
        Mockito.when(this.pnDeliveryClient.getSentNotificationPrivate(Mockito.any())).thenReturn(Mono.just(getSentNotificationV25Dto()));
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
        Mockito.when(this.dataVaultClient.anonymized(Mockito.any(), Mockito.any())).thenReturn(Mono.just("PF-4fc75df3-0913-407e-bdaa-e50329708b7d"));
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
        Mockito.when(this.pnDeliveryClient.getSentNotificationPrivate(Mockito.any())).thenReturn(Mono.just(getSentNotificationV25Dto()));
        NotificationDetailResponse response = notificationAndMessageService.getNotificationFromIUN("PRVZ-NZKM-JEDK-202309-A-1").block();
        Assertions.assertNotNull(response);
        Assertions.assertEquals("FRMTTR76M06B715E", response.getSenderTaxId());
        Assertions.assertEquals("31/12/2023", response.getPaymentExpirationDate());
        Assertions.assertEquals("comune", response.getSenderDenomination());
        Assertions.assertEquals("AR_REGISTERED_LETTER", response.getPhysicalCommunicationType().getValue());
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
        Mockito.when(this.pnDeliveryClient.searchNotificationsPrivate(Instant.parse("2023-08-15T15:49:05.630Z"),
                        Instant.parse("2023-08-31T15:49:05.630Z"), "PF-4fc75df3-0913-407e-bdaa-e50329708b7d",
                null, null, RecipientType.PF.getValue(), 50, "nextPageKey"))
                .thenReturn(Mono.just(getNotificationSearchResponseDto()));

        SearchNotificationsResponse response = notificationAndMessageService.searchNotificationsAsDelegateFromInternalId("", null, "PF-4fc75df3-0913-407e-bdaa-e50329708b7d", RecipientType.PF, 50,"nextPageKey", Instant.parse("2023-08-15T15:49:05.630Z"), Instant.parse("2023-08-31T15:49:05.630Z")).block();
        Assertions.assertNotNull(response);
        Assertions.assertEquals(1, response.getNextPagesKey().size());
        Assertions.assertEquals(1, response.getResults().size());
        Assertions.assertEquals(true, response.getMoreResult());


    }

    @Test
    void searchNotificationsAsDelegateFromInternalIdDeliveryClientError(){
        PnGenericException pnGenericException = new PnGenericException(ERROR_ON_DELIVERY_CLIENT, ERROR_ON_DELIVERY_CLIENT.getMessage());
        Mockito.when(this.pnDeliveryClient.searchNotificationsPrivate(Instant.parse("2023-08-15T15:49:05.630Z"),
                        Instant.parse("2023-08-31T15:49:05.630Z"), "PF-4fc75df3-0913-407e-bdaa-e50329708b7d",
                null, null, RecipientType.PF.getValue(), 50, "nextPageKey"))
                .thenReturn(Mono.error(pnGenericException));

        StepVerifier.create(notificationAndMessageService.searchNotificationsAsDelegateFromInternalId("", null, "PF-4fc75df3-0913-407e-bdaa-e50329708b7d", RecipientType.PF, 50, "nextPageKey", Instant.parse("2023-08-15T15:49:05.630Z"), Instant.parse("2023-08-31T15:49:05.630Z")))
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
                .NotificationStatusV26Dto.ACCEPTED);
        notificationSearchRowDto.setIun("iun123");
        notificationSearchRowDto.setGroup("group");
        notificationSearchRowDto.setSender("sender");
        notificationSearchRowDto.setSubject("subject");
        notificationSearchRowDto.setSentAt(Instant.now());
        notificationSearchRowDto.setMandateId("4321");
        notificationSearchRowDto.setPaProtocolNumber("1234");

        List<String> recipients = new ArrayList<>();
        recipients.add("456");

        notificationSearchRowDto.setRecipients(recipients);
        notificationSearchRowDto.setRequestAcceptedAt(Instant.now());
        notificationSearchRowDtoList.add(notificationSearchRowDto);

        notificationSearchResponseDto.setResultsPage(notificationSearchRowDtoList);

        return notificationSearchResponseDto;
    }

    private NotificationHistoryResponseDto getNotificationHistoryResponseDto() {
        NotificationHistoryResponseDto notificationHistoryResponseDto = new NotificationHistoryResponseDto();
        notificationHistoryResponseDto.setNotificationStatus(NotificationStatusV26Dto.ACCEPTED);

        List<TimelineElementV27Dto> timelineElementDtoList = new ArrayList<>();
        var timelineElementDto = new TimelineElementV27Dto();
        timelineElementDto.setCategory(TimelineElementCategoryV27Dto.SEND_COURTESY_MESSAGE);
        timelineElementDto.setElementId("elementId");
        timelineElementDto.setDetails(new TimelineElementDetailsV27Dto());
        timelineElementDto.setTimestamp(Instant.now());

        List<LegalFactListElementV20Dto> legalFactListElementDtoList = new ArrayList<>();
        LegalFactListElementV20Dto legalFactListElementDto = new LegalFactListElementV20Dto();
        legalFactListElementDto.setLegalFactsId(new LegalFactsIdV20Dto());
        legalFactListElementDto.setTaxId("taxId");
        legalFactListElementDto.setIun("iun123");
        legalFactListElementDtoList.add(legalFactListElementDto);

        notificationHistoryResponseDto.setTimeline(timelineElementDtoList);

        List<NotificationStatusHistoryElementV26Dto> notificationStatusHistoryElementDtoList = new ArrayList<>();
        var notificationStatusHistoryElementDto = new NotificationStatusHistoryElementV26Dto();
        notificationStatusHistoryElementDto.setStatus(NotificationStatusV26Dto.ACCEPTED);
        notificationStatusHistoryElementDto.setActiveFrom(Instant.now());

        List<String> relativeTimeLineElements = new ArrayList<>();
        relativeTimeLineElements.add("relativeTimeLineElement");
        notificationStatusHistoryElementDto.setRelatedTimelineElements(relativeTimeLineElements);
        notificationStatusHistoryElementDtoList.add(notificationStatusHistoryElementDto);

        notificationHistoryResponseDto.setNotificationStatusHistory(notificationStatusHistoryElementDtoList);

        return notificationHistoryResponseDto;
    }

    private SentNotificationV25Dto getSentNotificationV25Dto (){
        SentNotificationV25Dto sentNotificationV21Dto = new SentNotificationV25Dto();
        sentNotificationV21Dto.setSentAt(Instant.now());
        NotificationRecipientV24Dto notificationRecipientV21Dto = new NotificationRecipientV24Dto();
        notificationRecipientV21Dto.setRecipientType(NotificationRecipientV24Dto.RecipientTypeEnum.PF);
        notificationRecipientV21Dto.setPayments(new ArrayList<>());
        sentNotificationV21Dto.setPhysicalCommunicationType(SentNotificationV25Dto.PhysicalCommunicationTypeEnum.AR_REGISTERED_LETTER);
        sentNotificationV21Dto.setSenderDenomination("comune");
        sentNotificationV21Dto.setSenderTaxId("FRMTTR76M06B715E");
        sentNotificationV21Dto.setSentAt(Instant.now());
        sentNotificationV21Dto.setPaymentExpirationDate("31/12/2023");
        sentNotificationV21Dto.setRecipients(List.of(
                new NotificationRecipientV24Dto()
                        .recipientType(NotificationRecipientV24Dto.RecipientTypeEnum.PF)
                        .taxId("DVNLRD52D15M059P")
                        .denomination("Leo  denomination")
                        .payments(List.of(
                                new NotificationPaymentItemDto().pagoPa(new PagoPaPaymentDto().creditorTaxId("77777777777").noticeCode("302011730298073905").applyCost(false)),
                                new NotificationPaymentItemDto().f24(new F24PaymentDto().title("Titolo f24").applyCost(false))
                        ))
        ));
        return sentNotificationV21Dto;
    }

    private NotificationHistoryResponseDto getHistory (){
        NotificationHistoryResponseDto historyResponseDto = new NotificationHistoryResponseDto();
        historyResponseDto.setNotificationStatus(NotificationStatusV26Dto.ACCEPTED);
        var timelineElementDto = new TimelineElementV27Dto();
        timelineElementDto.setCategory(TimelineElementCategoryV27Dto.REQUEST_ACCEPTED);
        timelineElementDto.setDetails(new TimelineElementDetailsV27Dto());
        timelineElementDto.setTimestamp(Instant.now());
        List<TimelineElementV27Dto> dtoList = new ArrayList<>();
        dtoList.add(timelineElementDto);
        historyResponseDto.setTimeline(dtoList);
        return historyResponseDto;
    }

    private NotificationHistoryResponseDto getHistoryNotificationCancellation (){
        NotificationHistoryResponseDto historyResponseDto = new NotificationHistoryResponseDto();
        historyResponseDto.setNotificationStatus(NotificationStatusV26Dto.ACCEPTED);
        var timelineElementDto = new TimelineElementV27Dto();
        timelineElementDto.setCategory(TimelineElementCategoryV27Dto.NOTIFICATION_CANCELLATION_REQUEST);
        timelineElementDto.setDetails(new TimelineElementDetailsV27Dto());
        timelineElementDto.setTimestamp(Instant.now());
        List<TimelineElementV27Dto> dtoList = new ArrayList<>();
        dtoList.add(timelineElementDto);
        historyResponseDto.setTimeline(dtoList);
        return historyResponseDto;
    }


    @Test
    void getNotificationRecipientDetailOK(){
        var iun = "PRVZ-NZKM-JEDK-202309-A-1";
        var taxId = "DVNLRD52D15M059P";
        var notification = getSentNotificationV25Dto();
        var pagoPaPaymentExpected = notification.getRecipients().get(0).getPayments().get(0).getPagoPa();
        var creditorTaxIdExpected = pagoPaPaymentExpected.getCreditorTaxId();
        var noticeCodeExpected = pagoPaPaymentExpected.getNoticeCode();
        Mockito.when(this.pnDeliveryClient.getSentNotificationPrivate(iun)).thenReturn(Mono.just(notification));
        final PaymentInfoV21Dto paymentInfoExpected = new PaymentInfoV21Dto().amount(100).causaleVersamento("Causale").dueDate("2024-11-05").creditorTaxId(creditorTaxIdExpected).noticeCode(noticeCodeExpected).status(PaymentStatusDto.REQUIRED);
        Mockito.when(this.externalRegistriesClient.getPaymentInfo(List.of(new PaymentInfoRequestDto().noticeCode(noticeCodeExpected).creditorTaxId(creditorTaxIdExpected))))
                .thenReturn(Flux.just(paymentInfoExpected));
        var response = notificationAndMessageService.getNotificationRecipientDetail(iun, taxId).block();
        Assertions.assertNotNull(response);
        Assertions.assertEquals("FRMTTR76M06B715E", response.getSenderTaxId());
        Assertions.assertEquals("31/12/2023", response.getPaymentExpirationDate());
        Assertions.assertEquals("comune", response.getSenderDenomination());
        Assertions.assertEquals("AR_REGISTERED_LETTER", response.getPhysicalCommunicationType().getValue());

        var recipientResponseActual = response.getRecipient();
        var recipientExpected = notification.getRecipients().get(0);
        Assertions.assertEquals(recipientExpected.getRecipientType().getValue(), recipientResponseActual.getRecipientType().getValue());
        Assertions.assertEquals(recipientExpected.getDenomination(), recipientResponseActual.getDenomination());
        Assertions.assertEquals(creditorTaxIdExpected, recipientResponseActual.getPayments().get(0).getPagoPa().getCreditorTaxId());
        Assertions.assertEquals(noticeCodeExpected, recipientResponseActual.getPayments().get(0).getPagoPa().getNoticeCode());
        Assertions.assertEquals(paymentInfoExpected.getCausaleVersamento(), recipientResponseActual.getPayments().get(0).getPagoPa().getCausaleVersamento());
        Assertions.assertEquals(paymentInfoExpected.getDueDate(), recipientResponseActual.getPayments().get(0).getPagoPa().getDueDate());
        Assertions.assertEquals(paymentInfoExpected.getStatus().getValue(), recipientResponseActual.getPayments().get(0).getPagoPa().getStatus());
        Assertions.assertNull(recipientResponseActual.getPayments().get(0).getPagoPa().getErrorCode());
        Assertions.assertNull(recipientResponseActual.getPayments().get(0).getPagoPa().getDetail());
    }

    @Test
    void getNotificationRecipientDetailKOForIun(){
        var iun = "PRVZ-NZKM-JEDK-202309-A-1";
        var taxId = "DVNLRD52D15M059P";
        Mockito.when(this.pnDeliveryClient.getSentNotificationPrivate(iun)).thenReturn(Mono.error(WebClientResponseException.create(404, "Not Found", null, null, null)));
        StepVerifier.create(notificationAndMessageService.getNotificationRecipientDetail(iun, taxId))
                .expectErrorMatches(throwable -> throwable instanceof PnGenericException e && e.getHttpStatus().equals(HttpStatus.NOT_FOUND))
                .verify();

    }

    @Test
    void getNotificationRecipientDetailKOForTaxId(){
        var iun = "PRVZ-NZKM-JEDK-202309-A-1";
        var taxId = "AAAAAAAAAAAAAA";
        var notification = getSentNotificationV25Dto();
        Mockito.when(this.pnDeliveryClient.getSentNotificationPrivate(iun)).thenReturn(Mono.just(notification));
        StepVerifier.create(notificationAndMessageService.getNotificationRecipientDetail(iun, taxId))
                .expectErrorMatches(throwable -> throwable instanceof PnGenericException e && e.getHttpStatus().equals(HttpStatus.BAD_REQUEST))
                .verify();

    }

    @Test
    void getNotificationRecipientDetailPaymentInfoHttpKO(){
        var iun = "PRVZ-NZKM-JEDK-202309-A-1";
        var taxId = "DVNLRD52D15M059P";
        var notification = getSentNotificationV25Dto();
        var pagoPaPaymentExpected = notification.getRecipients().get(0).getPayments().get(0).getPagoPa();
        var creditorTaxIdExpected = pagoPaPaymentExpected.getCreditorTaxId();
        var noticeCodeExpected = pagoPaPaymentExpected.getNoticeCode();
        Mockito.when(this.pnDeliveryClient.getSentNotificationPrivate(iun)).thenReturn(Mono.just(notification));
        Mockito.when(this.externalRegistriesClient.getPaymentInfo(List.of(new PaymentInfoRequestDto().noticeCode(noticeCodeExpected).creditorTaxId(creditorTaxIdExpected))))
                .thenReturn(Flux.error(WebClientResponseException.create(503, "Service Unavailable", null, null, null)));
        var response = notificationAndMessageService.getNotificationRecipientDetail(iun, taxId).block();
        Assertions.assertNotNull(response);
        Assertions.assertEquals("FRMTTR76M06B715E", response.getSenderTaxId());
        Assertions.assertEquals("31/12/2023", response.getPaymentExpirationDate());
        Assertions.assertEquals("comune", response.getSenderDenomination());
        Assertions.assertEquals("AR_REGISTERED_LETTER", response.getPhysicalCommunicationType().getValue());

        var recipientResponseActual = response.getRecipient();
        var recipientExpected = notification.getRecipients().get(0);
        Assertions.assertEquals(recipientExpected.getRecipientType().getValue(), recipientResponseActual.getRecipientType().getValue());
        Assertions.assertEquals(recipientExpected.getDenomination(), recipientResponseActual.getDenomination());
        Assertions.assertEquals(creditorTaxIdExpected, recipientResponseActual.getPayments().get(0).getPagoPa().getCreditorTaxId());
        Assertions.assertEquals(noticeCodeExpected, recipientResponseActual.getPayments().get(0).getPagoPa().getNoticeCode());
        Assertions.assertNull(recipientResponseActual.getPayments().get(0).getPagoPa().getCausaleVersamento());
        Assertions.assertNull(recipientResponseActual.getPayments().get(0).getPagoPa().getDueDate());
        Assertions.assertNull(recipientResponseActual.getPayments().get(0).getPagoPa().getErrorCode());
        Assertions.assertNull(recipientResponseActual.getPayments().get(0).getPagoPa().getDetail());
    }

    @Test
    void getNotificationRecipientDetailPayment200WithErrorField(){
        var iun = "PRVZ-NZKM-JEDK-202309-A-1";
        var taxId = "DVNLRD52D15M059P";
        var notification = getSentNotificationV25Dto();
        var pagoPaPaymentExpected = notification.getRecipients().get(0).getPayments().get(0).getPagoPa();
        var creditorTaxIdExpected = pagoPaPaymentExpected.getCreditorTaxId();
        var noticeCodeExpected = pagoPaPaymentExpected.getNoticeCode();
        Mockito.when(this.pnDeliveryClient.getSentNotificationPrivate(iun)).thenReturn(Mono.just(notification));
        final PaymentInfoV21Dto paymentInfoExpected = new PaymentInfoV21Dto().errorCode("Error").detail(DetailDto.PAYMENT_DUPLICATED).creditorTaxId(creditorTaxIdExpected).noticeCode(noticeCodeExpected).status(PaymentStatusDto.SUCCEEDED);
        Mockito.when(this.externalRegistriesClient.getPaymentInfo(List.of(new PaymentInfoRequestDto().noticeCode(noticeCodeExpected).creditorTaxId(creditorTaxIdExpected))))
                .thenReturn(Flux.just(paymentInfoExpected));
        var response = notificationAndMessageService.getNotificationRecipientDetail(iun, taxId).block();
        Assertions.assertNotNull(response);
        Assertions.assertEquals("FRMTTR76M06B715E", response.getSenderTaxId());
        Assertions.assertEquals("31/12/2023", response.getPaymentExpirationDate());
        Assertions.assertEquals("comune", response.getSenderDenomination());
        Assertions.assertEquals("AR_REGISTERED_LETTER", response.getPhysicalCommunicationType().getValue());

        var recipientResponseActual = response.getRecipient();
        var recipientExpected = notification.getRecipients().get(0);
        Assertions.assertEquals(recipientExpected.getRecipientType().getValue(), recipientResponseActual.getRecipientType().getValue());
        Assertions.assertEquals(recipientExpected.getDenomination(), recipientResponseActual.getDenomination());
        Assertions.assertEquals(creditorTaxIdExpected, recipientResponseActual.getPayments().get(0).getPagoPa().getCreditorTaxId());
        Assertions.assertEquals(noticeCodeExpected, recipientResponseActual.getPayments().get(0).getPagoPa().getNoticeCode());
        Assertions.assertNull(recipientResponseActual.getPayments().get(0).getPagoPa().getCausaleVersamento());
        Assertions.assertNull(recipientResponseActual.getPayments().get(0).getPagoPa().getDueDate());
        Assertions.assertEquals(paymentInfoExpected.getStatus().getValue(), recipientResponseActual.getPayments().get(0).getPagoPa().getStatus());
        Assertions.assertEquals(paymentInfoExpected.getErrorCode(), recipientResponseActual.getPayments().get(0).getPagoPa().getErrorCode());
        Assertions.assertEquals(paymentInfoExpected.getDetail().getValue(), recipientResponseActual.getPayments().get(0).getPagoPa().getDetail());
    }

}
