package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationAttachmentDownloadMetadataResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationSearchRowDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationV23Dto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.NotificationHistoryResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.TimelineElementCategoryV23Dto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.TimelineElementV23Dto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import org.modelmapper.ModelMapper;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class NotificationAndMessageMapper {

    private NotificationAndMessageMapper(){}

    private static final ModelMapper modelMapper = new ModelMapper();

    public static NotificationResponse getNotification (NotificationSearchRowDto notificationSearchRowDto, List<TimelineElementV23Dto> filteredElements){
        NotificationResponse notification = new NotificationResponse();
        notification.setIun(notificationSearchRowDto.getIun());
        notification.setSender(notificationSearchRowDto.getSender());
        notification.setSentAt(notificationSearchRowDto.getSentAt());
        notification.setSubject(notificationSearchRowDto.getSubject());
        notification.setIunStatus(IunStatus.fromValue(notificationSearchRowDto.getNotificationStatus().getValue()));

            if (!CollectionUtils.isEmpty(filteredElements)){
            List<CourtesyMessage> courtesyMessages = new ArrayList<>();
            filteredElements.forEach(timelineElementDto ->
                    courtesyMessages.add(getCourtesyMessage(timelineElementDto))
            );
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

    private static List<TimelineElementV23Dto> filteredElements(List<TimelineElementV23Dto> timelineElementList){

        return timelineElementList
                .stream()
                .filter(element -> element.getCategory().equals(TimelineElementCategoryV23Dto.REQUEST_ACCEPTED) ||
                        element.getCategory().equals(TimelineElementCategoryV23Dto.SEND_COURTESY_MESSAGE) ||
                        element.getCategory().equals(TimelineElementCategoryV23Dto.SCHEDULE_DIGITAL_WORKFLOW) ||
                        element.getCategory().equals(TimelineElementCategoryV23Dto.SEND_DIGITAL_DOMICILE) ||
                        element.getCategory().equals(TimelineElementCategoryV23Dto.SEND_DIGITAL_PROGRESS) ||
                        element.getCategory().equals(TimelineElementCategoryV23Dto.SEND_DIGITAL_FEEDBACK) ||
                        element.getCategory().equals(TimelineElementCategoryV23Dto.DIGITAL_SUCCESS_WORKFLOW) ||
                        element.getCategory().equals(TimelineElementCategoryV23Dto.DIGITAL_FAILURE_WORKFLOW) ||
                        element.getCategory().equals(TimelineElementCategoryV23Dto.ANALOG_FAILURE_WORKFLOW) ||
                        element.getCategory().equals(TimelineElementCategoryV23Dto.SEND_SIMPLE_REGISTERED_LETTER) ||
                        element.getCategory().equals(TimelineElementCategoryV23Dto.NOTIFICATION_VIEWED) ||
                        element.getCategory().equals(TimelineElementCategoryV23Dto.PREPARE_ANALOG_DOMICILE_FAILURE) ||
                        element.getCategory().equals(TimelineElementCategoryV23Dto.SEND_ANALOG_DOMICILE) ||
                        element.getCategory().equals(TimelineElementCategoryV23Dto.SEND_ANALOG_PROGRESS) ||
                        element.getCategory().equals(TimelineElementCategoryV23Dto.SEND_ANALOG_FEEDBACK) ||
                        element.getCategory().equals(TimelineElementCategoryV23Dto.COMPLETELY_UNREACHABLE) ||
                        element.getCategory().equals(TimelineElementCategoryV23Dto.AAR_GENERATION) ||
                        element.getCategory().equals(TimelineElementCategoryV23Dto.NOT_HANDLED) ||
                        element.getCategory().equals(TimelineElementCategoryV23Dto.REFINEMENT)
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

    public static NotificationDetailResponse getNotificationDetail(SentNotificationV23Dto sentNotificationV21Dto) {
        NotificationDetailResponse notificationDetailResponse = new NotificationDetailResponse();
        notificationDetailResponse.setPaProtocolNumber(sentNotificationV21Dto.getPaProtocolNumber());
        notificationDetailResponse.setSubject(sentNotificationV21Dto.getSubject());
        notificationDetailResponse.setAbstract(sentNotificationV21Dto.getAbstract());
        notificationDetailResponse.setIsMultiRecipients(sentNotificationV21Dto.getRecipients().size() > 1);
        if(!sentNotificationV21Dto.getRecipients().isEmpty()){
            notificationDetailResponse.setHasPayments(!sentNotificationV21Dto.getRecipients().get(0).getPayments().isEmpty());
        }
        notificationDetailResponse.setAmount(sentNotificationV21Dto.getAmount());
        notificationDetailResponse.setHasDocuments(!sentNotificationV21Dto.getDocuments().isEmpty());
        notificationDetailResponse.setPhysicalCommunicationType(NotificationDetailResponse.PhysicalCommunicationTypeEnum
                .fromValue(sentNotificationV21Dto.getPhysicalCommunicationType().getValue()));
        notificationDetailResponse.setSenderDenomination(sentNotificationV21Dto.getSenderDenomination());
        notificationDetailResponse.setSenderTaxId(sentNotificationV21Dto.getSenderTaxId());
        notificationDetailResponse.setSentAt(sentNotificationV21Dto.getSentAt());
        notificationDetailResponse.setPaymentExpirationDate(sentNotificationV21Dto.getPaymentExpirationDate());
        return notificationDetailResponse;
    }


    private static CourtesyMessage getCourtesyMessage(TimelineElementV23Dto timelineElementV20Dto) {
        CourtesyMessage courtesyMessage = new CourtesyMessage();
        if (timelineElementV20Dto.getDetails() != null) {
            courtesyMessage.setChannel(CourtesyChannelType.fromValue(timelineElementV20Dto.getDetails().getDigitalAddress().getType()));
            courtesyMessage.setSentTimestamp(timelineElementV20Dto.getDetails().getSendDate());
        }
        return courtesyMessage;
    }
}
