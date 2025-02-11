package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.*;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationStatusDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.*;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class NotificationAndMessageMapperTest {

    private final NotificationSearchRowDto notificationSearchRowDto = new NotificationSearchRowDto();
    private final List<TimelineElementV26Dto> filteredElements = new ArrayList<>();
    private final NotificationHistoryResponseDto historyResponseDto = new NotificationHistoryResponseDto();
    private final NotificationAttachmentDownloadMetadataResponseDto notificationAttachmentDownloadMetadataResponseDto = new NotificationAttachmentDownloadMetadataResponseDto();
    private final SentNotificationV23Dto sentNotificationV21Dto = new SentNotificationV23Dto();

    @BeforeEach
    void initialize (){
        notificationSearchRowDto.setSender("Comune");
        notificationSearchRowDto.setIun("PRVZ-NZKM-JEDK-202309-A-1");
        notificationSearchRowDto.setSentAt(Instant.now());
        notificationSearchRowDto.setSubject("comune di Palermo");
        notificationSearchRowDto.setNotificationStatus(NotificationStatusDto.ACCEPTED);
        var timelineElementV23Dto = new TimelineElementV26Dto();
        var detailsV23Dto = new TimelineElementDetailsV26Dto();
        detailsV23Dto.setSendDate(Instant.now());
        timelineElementV23Dto.setDetails(detailsV23Dto);
        timelineElementV23Dto.setCategory(TimelineElementCategoryV26Dto.REQUEST_ACCEPTED);
        timelineElementV23Dto.setTimestamp(Instant.now());

        var refinement = new TimelineElementV26Dto();
        var refinementDetail = new TimelineElementDetailsV26Dto();
        detailsV23Dto.setSendDate(Instant.now().plusSeconds(10));
        refinement.setDetails(refinementDetail);
        refinement.setCategory(TimelineElementCategoryV26Dto.REFINEMENT);
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
        NotificationRecipientV23Dto notificationRecipientV21Dto = new NotificationRecipientV23Dto();
        notificationRecipientV21Dto.setRecipientType(NotificationRecipientV23Dto.RecipientTypeEnum.PF);
        notificationRecipientV21Dto.setPayments(new ArrayList<>());
        List<NotificationRecipientV23Dto> recipients = new ArrayList<>();
        recipients.add(notificationRecipientV21Dto);
        sentNotificationV21Dto.setRecipients(recipients);
        sentNotificationV21Dto.setAmount(1234);
        List<NotificationDocumentDto> documentDtoList = new ArrayList<>();
        NotificationDocumentDto documentDto = new NotificationDocumentDto();
        documentDtoList.add(documentDto);
        sentNotificationV21Dto.setDocuments(documentDtoList);
        sentNotificationV21Dto.setPhysicalCommunicationType(SentNotificationV23Dto.PhysicalCommunicationTypeEnum.REGISTERED_LETTER_890);
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

}
