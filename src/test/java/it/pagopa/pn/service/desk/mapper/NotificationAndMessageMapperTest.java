package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.*;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.*;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.NotificationStatusV26Dto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class NotificationAndMessageMapperTest {

    private final NotificationSearchRowDto notificationSearchRowDto = new NotificationSearchRowDto();
    private final List<TimelineElementV27Dto> filteredElements = new ArrayList<>();
    private final NotificationHistoryResponseDto historyResponseDto = new NotificationHistoryResponseDto();
    private final NotificationAttachmentDownloadMetadataResponseDto notificationAttachmentDownloadMetadataResponseDto = new NotificationAttachmentDownloadMetadataResponseDto();
    private final SentNotificationV25Dto sentNotificationV21Dto = new SentNotificationV25Dto();

    @BeforeEach
    void initialize (){
        notificationSearchRowDto.setSender("Comune");
        notificationSearchRowDto.setIun("PRVZ-NZKM-JEDK-202309-A-1");
        notificationSearchRowDto.setSentAt(Instant.now());
        notificationSearchRowDto.setSubject("comune di Palermo");
        notificationSearchRowDto.setNotificationStatus(it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationStatusV26Dto.ACCEPTED);
        var timelineElementV23Dto = new TimelineElementV27Dto();
        var detailsV23Dto = new TimelineElementDetailsV27Dto();
        detailsV23Dto.setSendDate(Instant.now());
        timelineElementV23Dto.setDetails(detailsV23Dto);
        timelineElementV23Dto.setCategory(TimelineElementCategoryV27Dto.REQUEST_ACCEPTED);
        timelineElementV23Dto.setTimestamp(Instant.now());

        var refinement = new TimelineElementV27Dto();
        var refinementDetail = new TimelineElementDetailsV27Dto();
        detailsV23Dto.setSendDate(Instant.now().plusSeconds(10));
        refinement.setDetails(refinementDetail);
        refinement.setCategory(TimelineElementCategoryV27Dto.REFINEMENT);
        refinement.setTimestamp(Instant.now().plusSeconds(10));

        DigitalAddressDto digitalAddressDto = new DigitalAddressDto();
        digitalAddressDto.setType("PEC");
        detailsV23Dto.setDigitalAddress(digitalAddressDto);
        filteredElements.add(timelineElementV23Dto);
        var allTimelines = List.of(timelineElementV23Dto, refinement);
        historyResponseDto.setTimeline(allTimelines);
        historyResponseDto.setNotificationStatus(NotificationStatusV26Dto.ACCEPTED);
        notificationAttachmentDownloadMetadataResponseDto.setFilename("document_test");
        notificationAttachmentDownloadMetadataResponseDto.setContentType("test");
        notificationAttachmentDownloadMetadataResponseDto.setContentLength(1200);
        sentNotificationV21Dto.setPaProtocolNumber("abc123");
        sentNotificationV21Dto.setSubject("comune di Palermo");
        sentNotificationV21Dto.setAbstract("");
        NotificationRecipientV24Dto notificationRecipientV21Dto = new NotificationRecipientV24Dto();
        notificationRecipientV21Dto.setRecipientType(NotificationRecipientV24Dto.RecipientTypeEnum.PF);
        notificationRecipientV21Dto.setPayments(new ArrayList<>());
        List<NotificationRecipientV24Dto> recipients = new ArrayList<>();
        recipients.add(notificationRecipientV21Dto);
        sentNotificationV21Dto.setRecipients(recipients);
        sentNotificationV21Dto.setAmount(1234);
        List<NotificationDocumentDto> documentDtoList = new ArrayList<>();
        NotificationDocumentDto documentDto = new NotificationDocumentDto();
        documentDtoList.add(documentDto);
        sentNotificationV21Dto.setDocuments(documentDtoList);
        sentNotificationV21Dto.setPhysicalCommunicationType(SentNotificationV25Dto.PhysicalCommunicationTypeEnum.REGISTERED_LETTER_890);
        sentNotificationV21Dto.setSenderDenomination("Fieramosca");
        sentNotificationV21Dto.setSenderTaxId("FRMTTR76M06B715E");
        sentNotificationV21Dto.setSentAt(Instant.now());
        sentNotificationV21Dto.setPaymentExpirationDate("");
    }

    @Test
    void getNotificationTest (){
        NotificationResponse notificationResponse = NotificationAndMessageMapper.getNotification(notificationSearchRowDto, filteredElements);
        assertNotNull(notificationResponse);
    }

    @Test
    void getTimeLineTest (){
        TimelineResponse response = NotificationAndMessageMapper.getTimeline(historyResponseDto);
        assertNotNull(response);
    }

    @Test
    void getDocument (){
        Document document = NotificationAndMessageMapper.getDocument(notificationAttachmentDownloadMetadataResponseDto);
        assertNotNull(document);
    }

    @Test
    void getNotificationDetailTest (){
        NotificationDetailResponse notificationDetailResponse = NotificationAndMessageMapper.getNotificationDetail(sentNotificationV21Dto);
        assertNotNull(notificationDetailResponse);
    }

    @Test
    void getTimelineWithRefinement() {
        TimelineResponse response = NotificationAndMessageMapper.getTimeline(historyResponseDto);
        assertNotNull(response);
        assertThat(response.getTimeline()).hasSize(2);
        var categories = response.getTimeline().stream().map(TimelineElement::getCategory).toList();
        assertThat(categories).contains(TimelineElementCategory.REFINEMENT);
    }

    @Test
    void getNotificationRecipientDetailResponseTest() {
        SentNotificationV25Dto sentNotification = getSentNotificationDto();

        String taxId = "TAX123";

        NotificationRecipientDetailResponse response = NotificationAndMessageMapper.getNotificationRecipientDetailResponse(sentNotification, taxId);

        assertNotNull(response);
        assertEquals("PA123", response.getPaProtocolNumber());
        assertEquals("Test Subject", response.getSubject());
        assertEquals("Test Abstract", response.getAbstract());
        assertEquals(1000, response.getAmount());
        assertEquals("John Doe", response.getRecipient().getDenomination());
        assertEquals("TAX123", response.getRecipient().getTaxId());
        assertEquals(NotificationRecipientDetailResponse.PhysicalCommunicationTypeEnum.REGISTERED_LETTER_890, response.getPhysicalCommunicationType());
    }

    @Test
    void getNotificationRecipientDetailResponseThrowsExceptionWhenTaxIdNotFound() {
        SentNotificationV25Dto sentNotification = getSentNotificationDto();
        sentNotification.setRecipients(getNotificationRecipientList());

        PnGenericException exception = assertThrows(PnGenericException.class, () ->
                NotificationAndMessageMapper.getNotificationRecipientDetailResponse(sentNotification, null)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    private List<NotificationRecipientV24Dto> getNotificationRecipientList() {
        NotificationRecipientV24Dto recipient = new NotificationRecipientV24Dto();
        recipient.setRecipientType(NotificationRecipientV24Dto.RecipientTypeEnum.PF);
        recipient.payments(new ArrayList<>());
        recipient.setDenomination("John Doe");
        recipient.setTaxId("TAX123");
        recipient.setPayments(new ArrayList<>());

        List<NotificationRecipientV24Dto> recipients = new ArrayList<>();
        recipients.add(recipient);
        return recipients;
    }

    private SentNotificationV25Dto getSentNotificationDto() {
        SentNotificationV25Dto sentNotification = new SentNotificationV25Dto();
        sentNotification.setPaProtocolNumber("PA123");
        sentNotification.setSubject("Test Subject");
        sentNotification.setAbstract("Test Abstract");
        sentNotification.setAmount(1000);

        sentNotification.setRecipients(getNotificationRecipientList());

        sentNotification.setDocuments(new ArrayList<>());
        sentNotification.setPhysicalCommunicationType(SentNotificationV25Dto.PhysicalCommunicationTypeEnum.REGISTERED_LETTER_890);
        sentNotification.setSenderDenomination("Sender Name");
        sentNotification.setSenderTaxId("SENDER123");
        return sentNotification;
    }

}
