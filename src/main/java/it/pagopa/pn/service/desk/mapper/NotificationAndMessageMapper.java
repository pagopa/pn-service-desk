package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationAttachmentDownloadMetadataResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationSearchRowDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationV21Dto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.NotificationHistoryResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.TimelineElementCategoryV20Dto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.TimelineElementV20Dto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;

public class NotificationAndMessageMapper {

    private NotificationAndMessageMapper(){}

    private static final ModelMapper modelMapper = new ModelMapper();

    public static NotificationResponse getNotification (NotificationSearchRowDto notificationSearchRowDto, List<TimelineElementV20Dto> filteredElements){
        NotificationResponse notification = new NotificationResponse();
        notification.setIun(notificationSearchRowDto.getIun());
        notification.setSender(notificationSearchRowDto.getSender());
        notification.setSentAt(notificationSearchRowDto.getSentAt());
        notification.setSubject(notificationSearchRowDto.getSubject());
        notification.setIunStatus(IunStatus.fromValue(notificationSearchRowDto.getNotificationStatus().getValue()));

        if (!filteredElements.isEmpty()){
            List<CourtesyMessage> courtesyMessages = new ArrayList<>();
            filteredElements.forEach(timelineElementDto -> {
                CourtesyMessage courtesyMessage = new CourtesyMessage();
                if (timelineElementDto.getDetails() != null){
                    courtesyMessage.setSentTimestamp(timelineElementDto.getDetails().getSendDate());
                }
                courtesyMessage.setChannel(CourtesyChannelType.fromValue(timelineElementDto.getDetails().getDigitalAddress().getType()));
                courtesyMessages.add(courtesyMessage);
            });
            notification.setCourtesyMessages(courtesyMessages);
        }
        return notification;
    }

    public static TimelineResponse getTimeline (NotificationHistoryResponseDto historyResponseDto){
        TimelineResponse response = new TimelineResponse();

        List<TimelineElement> timelineElementList = new ArrayList<>();
        if (historyResponseDto.getTimeline() != null && !historyResponseDto.getTimeline().isEmpty()) {
            filteredElements(historyResponseDto.getTimeline()).forEach(timelineElementDto -> {
                TimelineElement timelineElement = new TimelineElement();
                timelineElement.setCategory(TimelineElementCategory.fromValue(timelineElementDto.getCategory().getValue()));
                timelineElement.setDetail(modelMapper.map(timelineElementDto.getDetails(), TimelineElementDetail.class));
                timelineElement.setTimestamp(timelineElementDto.getTimestamp());
                timelineElementList.add(timelineElement);
            });
        }

        response.setTimeline(timelineElementList);
        response.setIunStatus(IunStatus.fromValue(historyResponseDto.getNotificationStatus().getValue()));

        return response;
    }

    private static List<TimelineElementV20Dto> filteredElements(List<TimelineElementV20Dto> timelineElementList){

        return timelineElementList
                .stream()
                .filter(element -> element.getCategory().equals(TimelineElementCategoryV20Dto.REQUEST_ACCEPTED) ||
                        element.getCategory().equals(TimelineElementCategoryV20Dto.SEND_COURTESY_MESSAGE) ||
                        element.getCategory().equals(TimelineElementCategoryV20Dto.SCHEDULE_DIGITAL_WORKFLOW) ||
                        element.getCategory().equals(TimelineElementCategoryV20Dto.SEND_DIGITAL_DOMICILE) ||
                        element.getCategory().equals(TimelineElementCategoryV20Dto.SEND_DIGITAL_PROGRESS) ||
                        element.getCategory().equals(TimelineElementCategoryV20Dto.SEND_DIGITAL_FEEDBACK) ||
                        element.getCategory().equals(TimelineElementCategoryV20Dto.DIGITAL_SUCCESS_WORKFLOW) ||
                        element.getCategory().equals(TimelineElementCategoryV20Dto.DIGITAL_FAILURE_WORKFLOW) ||
                        element.getCategory().equals(TimelineElementCategoryV20Dto.ANALOG_FAILURE_WORKFLOW) ||
                        element.getCategory().equals(TimelineElementCategoryV20Dto.SEND_SIMPLE_REGISTERED_LETTER) ||
                        element.getCategory().equals(TimelineElementCategoryV20Dto.NOTIFICATION_VIEWED) ||
                        element.getCategory().equals(TimelineElementCategoryV20Dto.PREPARE_ANALOG_DOMICILE_FAILURE) ||
                        element.getCategory().equals(TimelineElementCategoryV20Dto.SEND_ANALOG_DOMICILE) ||
                        element.getCategory().equals(TimelineElementCategoryV20Dto.SEND_ANALOG_PROGRESS) ||
                        element.getCategory().equals(TimelineElementCategoryV20Dto.SEND_ANALOG_FEEDBACK) ||
                        element.getCategory().equals(TimelineElementCategoryV20Dto.COMPLETELY_UNREACHABLE) ||
                        element.getCategory().equals(TimelineElementCategoryV20Dto.AAR_GENERATION) ||
                        element.getCategory().equals(TimelineElementCategoryV20Dto.NOT_HANDLED)
                )
                .toList();
    }

    public static Document getDocument(NotificationAttachmentDownloadMetadataResponseDto responseDto){
        Document document = new Document();
        document.setFilename(responseDto.getFilename());
        document.setContentLength(responseDto.getContentLength());
        document.setContentType(responseDto.getContentType());
        return document;
    }

    public static NotificationDetailResponse getNotificationDetail(SentNotificationV21Dto SentNotificationV21Dto) {
        NotificationDetailResponse notificationDetailResponse = new NotificationDetailResponse();
        notificationDetailResponse.setPaProtocolNumber(SentNotificationV21Dto.getPaProtocolNumber());
        notificationDetailResponse.setSubject(SentNotificationV21Dto.getSubject());
        notificationDetailResponse.setAbstract(SentNotificationV21Dto.getAbstract());
        notificationDetailResponse.setIsMultiRecipients(!SentNotificationV21Dto.getRecipients().isEmpty());
        if(!SentNotificationV21Dto.getRecipients().isEmpty()){
            notificationDetailResponse.setHasPayments(!SentNotificationV21Dto.getRecipients().get(0).getPayments().isEmpty());
        }
        notificationDetailResponse.setAmount(SentNotificationV21Dto.getAmount());
        notificationDetailResponse.setHasDocuments(!SentNotificationV21Dto.getDocuments().isEmpty());
        notificationDetailResponse.setPhysicalCommunicationType(NotificationDetailResponse.PhysicalCommunicationTypeEnum
                .fromValue(SentNotificationV21Dto.getPhysicalCommunicationType().getValue()));
        notificationDetailResponse.setSenderDenomination(SentNotificationV21Dto.getSenderDenomination());
        notificationDetailResponse.setSenderTaxId(SentNotificationV21Dto.getSenderTaxId());
        notificationDetailResponse.setSentAt(SentNotificationV21Dto.getSentAt());
        notificationDetailResponse.setPaymentExpirationDate(SentNotificationV21Dto.getPaymentExpirationDate());
        return notificationDetailResponse;
    }

    public static NotificationResponse getNotificationResponse(NotificationSearchRowDto notificationSearchRowDto, NotificationHistoryResponseDto notificationHistoryResponseDto) {
        List<CourtesyMessage> courtesyMessages = new ArrayList<>();
        if (notificationHistoryResponseDto.getTimeline() != null) {
            notificationHistoryResponseDto.getTimeline().forEach(timelineElementV20Dto ->
                    courtesyMessages.add(NotificationAndMessageMapper.getCourtesyMessage(timelineElementV20Dto))
            );
        }
        NotificationResponse notificationResponse = new NotificationResponse();
        notificationResponse.setIun(notificationSearchRowDto.getIun());
        notificationResponse.setSender(notificationSearchRowDto.getSender());
        notificationResponse.setSubject(notificationSearchRowDto.getSubject());
        notificationResponse.setIunStatus(IunStatus.fromValue(notificationSearchRowDto.getNotificationStatus().getValue()));
        notificationResponse.setCourtesyMessages(courtesyMessages);
        notificationResponse.setSentAt(notificationSearchRowDto.getSentAt());
        return notificationResponse;
    }

    public static CourtesyMessage getCourtesyMessage(TimelineElementV20Dto timelineElementV20Dto) {
        CourtesyMessage courtesyMessage = new CourtesyMessage();
        if (timelineElementV20Dto.getDetails() != null) {
            courtesyMessage.setChannel(CourtesyChannelType.fromValue(timelineElementV20Dto.getDetails().getDigitalAddress().getType()));
            courtesyMessage.setSentTimestamp(timelineElementV20Dto.getDetails().getSendDate());
        }
        return courtesyMessage;
    }
}
