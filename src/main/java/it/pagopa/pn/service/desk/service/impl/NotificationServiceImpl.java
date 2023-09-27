package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationsUnreachableResponse;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault.PnDataVaultClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClient;
import it.pagopa.pn.service.desk.service.NotificationService;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_GET_UNREACHABLE_NOTIFICATION;

@Service
@CustomLog
public class NotificationServiceImpl extends BaseService implements NotificationService {

    private final PnDataVaultClient dataVaultClient;
    private final PnDeliveryPushClient pnDeliveryPushClient;

    public NotificationServiceImpl(OperationDAO operationDAO, PnDataVaultClient dataVaultClient, PnDeliveryPushClient pnDeliveryPushClient) {
        super(operationDAO);
        this.dataVaultClient = dataVaultClient;
        this.pnDeliveryPushClient = pnDeliveryPushClient;
    }

    @Override
    public Mono<NotificationsUnreachableResponse> getUnreachableNotification(String xPagopaPnUid, NotificationRequest notificationRequest) {
        log.debug("xPagopaPnUid = {}, notificationRequest = {}, GetUnreachableNotification received input", xPagopaPnUid, notificationRequest);

        NotificationsUnreachableResponse notificationsUnreachableResponse = new NotificationsUnreachableResponse();
        return dataVaultClient.anonymized(notificationRequest.getTaxId())
                .flatMap(taxId -> this.pnDeliveryPushClient.paperNotificationFailed(taxId)
                        .onErrorResume(ex -> {
                            log.error("Paper notification failer error {}", ex);
                            return Mono.error(new PnGenericException(ERROR_GET_UNREACHABLE_NOTIFICATION, ERROR_GET_UNREACHABLE_NOTIFICATION.getMessage()));
                        })
                        .collectList()
                        .flatMap(notifications -> checkNotificationFailedCount(taxId, notifications.stream()
                                .map(e -> e.getIun())
                                .toList()))
                        .map(count -> {
                            notificationsUnreachableResponse.setNotificationsCount(count);
                            log.info("Unreachable notification: {} ", notificationsUnreachableResponse);
                            return notificationsUnreachableResponse;
                        })
                );
    }

}
