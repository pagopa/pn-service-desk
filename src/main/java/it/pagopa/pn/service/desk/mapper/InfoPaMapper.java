package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.LegalNotificationSearchResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.IunStatus;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationResponse;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.SearchNotificationsResponse;

import java.util.ArrayList;
import java.util.List;

public class InfoPaMapper {

    private InfoPaMapper(){}

    public static SearchNotificationsResponse getSearchNotificationResponse(LegalNotificationSearchResponseDto LegalNotificationSearchResponseDto) {
        SearchNotificationsResponse searchNotificationsResponse = new SearchNotificationsResponse();
        searchNotificationsResponse.setNextPagesKey(LegalNotificationSearchResponseDto.getNextPagesKey());
        searchNotificationsResponse.setMoreResult(LegalNotificationSearchResponseDto.getMoreResult());

        List<NotificationResponse> notificationResponses = new ArrayList<>();
        LegalNotificationSearchResponseDto.getResultsPage().forEach(LegalNotificationSearchRowDto -> {
            NotificationResponse notificationResponse = new NotificationResponse();
            notificationResponse.setSubject(LegalNotificationSearchRowDto.getSubject());
            notificationResponse.setSentAt(LegalNotificationSearchRowDto.getSentAt());
            notificationResponse.setIun(LegalNotificationSearchRowDto.getIun());
            notificationResponse.setSender(LegalNotificationSearchRowDto.getSender());
            if (LegalNotificationSearchRowDto.getNotificationStatus() != null) {
                notificationResponse.setIunStatus(IunStatus.fromValue(LegalNotificationSearchRowDto.getNotificationStatus().getValue()));
            }
            notificationResponse.setCourtesyMessages(new ArrayList<>());
            notificationResponses.add(notificationResponse);
        });
        searchNotificationsResponse.setResults(notificationResponses);

        return searchNotificationsResponse;
    }

}
