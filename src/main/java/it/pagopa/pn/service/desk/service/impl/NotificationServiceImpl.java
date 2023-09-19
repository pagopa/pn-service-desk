package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.ResponsePaperNotificationFailedDtoDto;
import it.pagopa.pn.service.desk.generated.openapi.pnraddfsu.v1.dto.ResponseStatusDto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationsUnreachableResponse;

import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault.PnDataVaultClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClient;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import it.pagopa.pn.service.desk.service.NotificationService;
import it.pagopa.pn.service.desk.utility.Utility;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.util.*;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.OPERATION_IS_NOT_PRESENT;
import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_GET_UNREACHABLE_NOTIFICATION;


import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_ON_RADD_INQUIRY;


@Service
@CustomLog
@AllArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private static final String RECIPIENT_TYPE = "PF";
    private static final Boolean IS_KO= false;

    private PnDataVaultClient dataVaultClient;
    private OperationDAO operationDAO;
    private PnDeliveryPushClient pnDeliveryPushClient;


    @Override
    public Mono<NotificationsUnreachableResponse> getUnreachableNotification(String xPagopaPnUid, NotificationRequest notificationRequest) {
        log.debug("xPagopaPnUid = {}, notificationRequest = {}, GetUnreachableNotification received input", xPagopaPnUid, notificationRequest);

        NotificationsUnreachableResponse notificationsUnreachableResponse = new NotificationsUnreachableResponse();
        String randomUUID = UUID.randomUUID().toString();

        return dataVaultClient.anonymized(notificationRequest.getTaxId())
                .flatMap(taxId -> this.pnDeliveryPushClient.paperNotificationFailed(taxId)
                        .onErrorResume(ex -> Mono.error(new PnGenericException(ERROR_GET_UNREACHABLE_NOTIFICATION,ex.getMessage())))
                        .collectList()
                        .flatMap(notifications -> checkNotificationFailed(taxId, notifications))
                        .doOnNext(op -> log.info("Operations: {}",op))
                        .map(result -> {
                            if(result)
                                notificationsUnreachableResponse.setNotificationsCount(1L);
                            else
                                notificationsUnreachableResponse.setNotificationsCount(0L);
                            log.info("Risultato notifiche in errore: {} ",notificationsUnreachableResponse);
                            return notificationsUnreachableResponse;
                        })
                );
    }

    private Mono<Boolean> checkNotificationFailed(String taxId, List<ResponsePaperNotificationFailedDtoDto> notifications) {
        return this.operationDAO.searchOperationsFromRecipientInternalId(taxId)
                .collectList()
                .flatMap(operations -> {
                    if (operations.isEmpty()) {
                        return Mono.just(true);
                    } else {
                        return Utility.operationContainsIun(operations, notifications);
                    }
                });
    }
}
