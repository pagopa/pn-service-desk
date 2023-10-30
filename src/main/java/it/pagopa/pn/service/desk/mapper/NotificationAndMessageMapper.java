package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationSearchRowDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.NotificationHistoryResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.TimelineElementCategoryV20Dto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.TimelineElementV20Dto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationAndMessageMapper {

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
                .collect(Collectors.toList());
    }
}
