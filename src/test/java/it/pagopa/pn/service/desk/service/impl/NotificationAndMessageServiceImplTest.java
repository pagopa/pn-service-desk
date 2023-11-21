package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationSearchResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationSearchRowDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.*;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.IunStatus;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.RecipientType;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.SearchNotificationsRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.SearchNotificationsResponse;
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

public class NotificationAndMessageServiceImplTest extends BaseTest {

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

}
