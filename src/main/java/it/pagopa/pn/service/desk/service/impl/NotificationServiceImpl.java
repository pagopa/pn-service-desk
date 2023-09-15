package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.NotificationsUnreachableResponse;

import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault.PnDataVaultClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClient;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import it.pagopa.pn.service.desk.service.NotificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.util.HashSet;
import java.util.Set;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.OPERATION_IS_NOT_PRESENT;
import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_GET_UNREACHABLE_NOTIFICATION;


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
                        .flatMap(notificationFailed -> checkNotificationFailed(notificationFailed.getRecipientInternalId(), notificationFailed.getIun()))
                        .onErrorResume(ex -> Mono.error(new PnGenericException(ERROR_GET_UNREACHABLE_NOTIFICATION,ex.getMessage())))
                        .distinct(PnServiceDeskOperations::getOperationId)
                        .doOnNext(op -> log.info("Operations: {}",op))
                        .collectList()
                        .map(operations -> {
                            notificationsUnreachableResponse.setNotificationsCount(Long.valueOf(operations.size()));
                            log.info("Notifiche effettive in errore: {} ",notificationsUnreachableResponse);
                            return notificationsUnreachableResponse;
                        })
                );
    }

    private Flux<PnServiceDeskOperations> checkNotificationFailed(String taxId, String iun) {
        return this.operationDAO.searchOperationsFromRecipientInternalId(taxId)
                .switchIfEmpty(Mono.error(new PnGenericException(OPERATION_IS_NOT_PRESENT, OPERATION_IS_NOT_PRESENT.getMessage())))
                .flatMap(operations -> operationContainsIun(operations,iun))
                .filter(operation -> operation.getStatus().equals(OperationStatusEnum.KO.toString()))
                .collectList()
                .flatMapMany(Flux::fromIterable);
    }

    private Flux<PnServiceDeskOperations> operationContainsIun(PnServiceDeskOperations operation,String iun){
        Set<PnServiceDeskOperations> listOperation = new HashSet<>();
        if(operation.getAttachments()!=null){
            for(PnServiceDeskAttachments attachments : operation.getAttachments()){
                if(attachments.getIun().equals(iun))
                    listOperation.add(operation);
            }
        }
        return Flux.fromIterable(listOperation);
    }
}
