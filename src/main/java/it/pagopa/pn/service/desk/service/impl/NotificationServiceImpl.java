package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.pnraddfsu.v1.dto.ResponseStatusDto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationsUnreachableResponse;

import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.raddfsu.PnRaddFsuClient;
import it.pagopa.pn.service.desk.service.NotificationService;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_ON_RADD_INQUIRY;


@Service
@CustomLog
@AllArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private static final String RECIPIENT_TYPE = "PF";

    private PnRaddFsuClient raddFsuClient;



    @Override
    public Mono<NotificationsUnreachableResponse> getUnreachableNotification(String xPagopaPnUid, NotificationRequest notificationRequest) {
        log.debug("xPagopaPnUid = {}, notificationRequest = {}, GetUnreachableNotification received input", xPagopaPnUid, notificationRequest);

        NotificationsUnreachableResponse notificationsUnreachableResponse = new NotificationsUnreachableResponse();
        String randomUUID = UUID.randomUUID().toString();

        log.debug("randomUUID = {}, taxId = {}, recipientType = {}, Retrieving unreachable notification via api inquiry", randomUUID, notificationRequest.getTaxId(), RECIPIENT_TYPE);
        return raddFsuClient.aorInquiry(randomUUID, notificationRequest.getTaxId(), RECIPIENT_TYPE)
                .map(aorInquiryResponse -> {
                    log.debug("aorInquiryResponse = {}, Are there unreachable notification?", aorInquiryResponse);
                    if (Boolean.TRUE.equals(aorInquiryResponse.getResult())
                            && Objects.equals(Objects.requireNonNull(aorInquiryResponse.getStatus()).getCode(), ResponseStatusDto.CodeEnum.NUMBER_0)) {
                        log.debug("aorInquiryResponseResult = {}, There are unreachable notification", aorInquiryResponse.getResult());
                        notificationsUnreachableResponse.setNotificationsCount(1L);
                    } else {
                        log.debug("aorInquiryResponseResult = {}, There are not unreachable notification", aorInquiryResponse.getResult());
                        notificationsUnreachableResponse.setNotificationsCount(0L);
                    }
                    return notificationsUnreachableResponse;
                })
                .onErrorResume(exception -> {
                    log.error("errorReason = {}, An error occurred while calling the service inquiry api", exception.getMessage());
                    return Mono.error(new PnGenericException(ERROR_ON_RADD_INQUIRY, ERROR_ON_RADD_INQUIRY.getMessage()));
                });
    }
}
