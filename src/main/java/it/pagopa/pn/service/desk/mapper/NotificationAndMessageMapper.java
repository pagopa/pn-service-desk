package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationSearchRowDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.TimelineElementDto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;

import java.util.ArrayList;
import java.util.List;

public class NotificationAndMessageMapper {

    public static Notification getNotification (NotificationSearchRowDto notificationSearchRowDto, List<TimelineElementDto> filteredElements){
        Notification notification = new Notification();
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
}
