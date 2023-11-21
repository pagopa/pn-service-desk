package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationSearchResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.IunStatus;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationResponse;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.SearchNotificationsResponse;

import java.util.ArrayList;
import java.util.List;

public class InfoPaMapper {

    public static SearchNotificationsResponse getSearchNotificationResponse(NotificationSearchResponseDto notificationSearchResponseDto) {
        SearchNotificationsResponse searchNotificationsResponse = new SearchNotificationsResponse();
        searchNotificationsResponse.setNextPagesKey(notificationSearchResponseDto.getNextPagesKey());
        searchNotificationsResponse.setMoreResult(notificationSearchResponseDto.getMoreResult());

        List<NotificationResponse> notificationResponses = new ArrayList<>();
        notificationSearchResponseDto.getResultsPage().forEach(notificationSearchRowDto -> {
            NotificationResponse notificationResponse = new NotificationResponse();
            notificationResponse.setSubject(notificationSearchRowDto.getSubject());
            notificationResponse.setSentAt(notificationSearchRowDto.getSentAt());
            notificationResponse.setIun(notificationSearchRowDto.getIun());
            notificationResponse.setSender(notificationSearchRowDto.getSender());
            if (notificationSearchRowDto.getNotificationStatus() != null) {
                notificationResponse.setIunStatus(IunStatus.fromValue(notificationSearchRowDto.getNotificationStatus().getValue()));
            }
            notificationResponse.setCourtesyMessages(new ArrayList<>());
            notificationResponses.add(notificationResponse);
        });
        searchNotificationsResponse.setResults(notificationResponses);

        return searchNotificationsResponse;
    }

}
