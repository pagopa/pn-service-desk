package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.generated.openapi.pnraddfsu.v1.dto.ResponseStatusDto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationsUnreachableResponse;
import it.pagopa.pn.service.desk.middleware.msclient.RaddFsuClient;
import it.pagopa.pn.service.desk.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private RaddFsuClient raddFsuClient;

    private static final String RECIPIENT_TYPE = "PF";

    @Override
    public Mono<NotificationsUnreachableResponse> getUnreachableNotification(String xPagopaPnUid, NotificationRequest notificationRequest) {

        NotificationsUnreachableResponse notificationsUnreachableResponse = new NotificationsUnreachableResponse();

        return raddFsuClient.aorInquiry(UUID.randomUUID().toString(), notificationRequest.getTaxId(), RECIPIENT_TYPE)
                .map(aorInquiryResponse -> {
                    if (Boolean.TRUE.equals(aorInquiryResponse.getResult())
                            && Objects.equals(Objects.requireNonNull(aorInquiryResponse.getStatus()).getCode(), ResponseStatusDto.CodeEnum.NUMBER_0)){
                        notificationsUnreachableResponse.setNotificationsCount(0L);
                    }else{
                        notificationsUnreachableResponse.setNotificationsCount(1L);
                    }
                    return notificationsUnreachableResponse;
                });
    }
}
