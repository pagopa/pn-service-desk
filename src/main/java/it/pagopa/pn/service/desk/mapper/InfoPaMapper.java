package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.LegalNotificationSearchResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.IunStatus;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationResponse;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.SearchNotificationsResponse;

import java.util.ArrayList;
import java.util.List;

public class InfoPaMapper {

    private InfoPaMapper(){}

    public static SearchNotificationsResponse getSearchNotificationResponse(LegalNotificationSearchResponseDto legalNotificationSearchResponseDto) {
        SearchNotificationsResponse searchNotificationsResponse = new SearchNotificationsResponse();
        searchNotificationsResponse.setNextPagesKey(legalNotificationSearchResponseDto.getNextPagesKey());
        searchNotificationsResponse.setMoreResult(legalNotificationSearchResponseDto.getMoreResult());

        List<NotificationResponse> notificationResponses = new ArrayList<>();
        legalNotificationSearchResponseDto.getResultsPage().forEach(legalNotificationSearchRowDto -> {
            NotificationResponse notificationResponse = new NotificationResponse();
            notificationResponse.setSubject(legalNotificationSearchRowDto.getSubject());
            notificationResponse.setSentAt(legalNotificationSearchRowDto.getSentAt());
            notificationResponse.setIun(legalNotificationSearchRowDto.getIun());
            notificationResponse.setSender(legalNotificationSearchRowDto.getSender());
            if (legalNotificationSearchRowDto.getNotificationStatus() != null) {
                notificationResponse.setIunStatus(IunStatus.fromValue(legalNotificationSearchRowDto.getNotificationStatus().getValue()));
            }
            notificationResponse.setCourtesyMessages(new ArrayList<>());
            notificationResponses.add(notificationResponse);
        });
        searchNotificationsResponse.setResults(notificationResponses);

        return searchNotificationsResponse;
    }

}
