package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.ResponsePaperNotificationFailedDtoDto;
import it.pagopa.pn.service.desk.generated.openapi.pnraddfsu.v1.dto.ResponseStatusDto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationsUnreachableResponse;

import it.pagopa.pn.service.desk.mapper.AddressMapper;
import it.pagopa.pn.service.desk.mapper.OperationMapper;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault.PnDataVaultClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClient;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import it.pagopa.pn.service.desk.service.NotificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@AllArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private static final String RECIPIENT_TYPE = "PF";

    private PnDataVaultClient dataVaultClient;
    private OperationDAO operationDAO;
    private PnDeliveryPushClient pnDeliveryPushClient;



    @Override
    public Mono<NotificationsUnreachableResponse> getUnreachableNotification(String xPagopaPnUid, NotificationRequest notificationRequest) {

        NotificationsUnreachableResponse notificationsUnreachableResponse = new NotificationsUnreachableResponse();

        return dataVaultClient.anonymized(notificationRequest.getTaxId())
                .flatMap(taxId -> this.pnDeliveryPushClient.paperNotificationFailed(taxId)
                        .flatMap(notificationFailed -> checkNotificationFailed(notificationFailed.getRecipientInternalId()))
                        .collectList()
                        .map(operations -> {
                            notificationsUnreachableResponse.setNotificationsCount(Long.valueOf(operations.size()));
                            log.info("Notifiche effettive in errore: {} ",notificationsUnreachableResponse);
                            return notificationsUnreachableResponse;
                        }));
    }

    private Flux<PnServiceDeskOperations> checkNotificationFailed(String taxId) {
        return this.operationDAO.searchOperationsFromRecipientInternalId(taxId)
                .filter(operation -> operation.getStatus() != OperationStatusEnum.OK.toString() &&
                        operation.getStatus() != OperationStatusEnum.PROGRESS.toString())
                .collectList()
                .flatMapMany(Flux::fromIterable);
    }

}
