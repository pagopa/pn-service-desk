package it.pagopa.pn.service.desk.action.impl;

import it.pagopa.pn.service.desk.action.NotifyDeliveryPushAction;
import it.pagopa.pn.service.desk.action.common.CommonAction;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.exception.PnEntityNotFoundException;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClient;
import it.pagopa.pn.service.desk.middleware.queue.model.InternalEventBody;
import it.pagopa.pn.service.desk.middleware.queue.producer.InternalQueueMomProducer;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@CustomLog
@AllArgsConstructor
public class NotifyDeliveryPushActionImpl extends CommonAction implements NotifyDeliveryPushAction {

    private PnDeliveryPushClient pnDeliveryPushClient;
    private OperationDAO operationDAO;
    private InternalQueueMomProducer internalQueueMomProducer;
    private PnServiceDeskConfigs pnServiceDeskConfigs;

    @Override
    public void execute(InternalEventBody internalEventBody) {
        log.info("NotifyDeliveryPushActionImpl execute attempt nro {} for operationId {}", internalEventBody.getAttempt(), internalEventBody.getOperationId());
        if (internalEventBody.getIuns() == null || internalEventBody.getIuns().isEmpty()) {
            // attempt has finished and all iuns are notified
            updateOperationWithStatus(internalEventBody.getOperationId()).block();
            return;
        }

        if (pnServiceDeskConfigs.getNotifyAttempt() >= internalEventBody.getAttempt()) {
            log.info("call notifyNotificationViewed with iun {}", internalEventBody.getIuns().get(0));

            pnDeliveryPushClient.notifyNotificationViewed(internalEventBody.getIuns().get(0), internalEventBody.getOperationId(), internalEventBody.getRecipientInternalId())
                    .switchIfEmpty(Mono.defer(() -> {
                        log.error("an error occurs in notifyNotificationViewed with iun {}", internalEventBody.getIuns().get(0));
                        if (pnServiceDeskConfigs.getNotifyAttempt() < internalEventBody.getAttempt()+1) {
                            log.warn("attempt has finished for iun {}", internalEventBody.getIuns().get(0));
                            updateOperationAttachments(internalEventBody.getOperationId(), Boolean.FALSE, internalEventBody.getIuns().get(0));

                            // remove elements from queue
                            internalQueueMomProducer.push
                                    (getInternalEvent(popIun(internalEventBody.getIuns()), internalEventBody.getOperationId(),
                                            internalEventBody.getRecipientInternalId()));
                        } else {
                            // try another attempt
                            internalQueueMomProducer.push
                                    (getInternalEvent(internalEventBody.getIuns(), internalEventBody.getOperationId(),
                                            internalEventBody.getRecipientInternalId(), internalEventBody.getAttempt()+1));

                        }
                        return Mono.empty();
                    }))
                    .flatMap(response -> {
                        updateOperationAttachments(internalEventBody.getOperationId(), Boolean.TRUE, internalEventBody.getIuns().get(0));
                        internalQueueMomProducer.push(getInternalEvent(popIun(internalEventBody.getIuns()), internalEventBody.getOperationId(),
                                internalEventBody.getRecipientInternalId()));
                        return Mono.empty();
                    })
                    .block();
        }
    }

    private List<String> popIun(List<String> iuns) {
        return !iuns.isEmpty() ? iuns.subList(1, iuns.size()) : new ArrayList<>();
    }

    private Mono<Void> updateOperationAttachments(String operationId, Boolean notified, String iun) {
        Mono.just("").publishOn(Schedulers.boundedElastic())
                .flatMap(y -> operationDAO.getByOperationId(operationId)
                        .switchIfEmpty(Mono.error(new PnEntityNotFoundException()))
                        .flatMap(entityOperation -> {
                            List<PnServiceDeskAttachments> attachmentsList = entityOperation.getAttachments();
                            attachmentsList.replaceAll(attachments -> {
                                if (StringUtils.equals(attachments.getIun(), iun)) {
                                    attachments.setIsNotified(notified);
                                }
                                return attachments;
                            });
                            entityOperation.setAttachments(attachmentsList);
                            log.info("update notify view {} for iun: {}", notified.booleanValue(), iun);
                            return this.operationDAO.updateEntity(entityOperation);

                        }))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
        return Mono.empty();
    }

    private Mono<Void> updateOperationWithStatus(String operationId) {
        log.info("update status for operationId: {}", operationId);
        return operationDAO.getByOperationId(operationId)
                .switchIfEmpty(Mono.error(new PnEntityNotFoundException()))
                .flatMap(entityOperation -> {
                    if (entityOperation.getAttachments().stream()
                            .filter(attachments -> attachments.getIsNotified() == Boolean.FALSE)
                            .count() > 0) {
                        entityOperation.setStatus(OperationStatusEnum.NOTIFY_VIEW_ERROR.toString());
                        entityOperation.setErrorReason("Error during Notify View Flow");
                    } else {
                        entityOperation.setStatus(OperationStatusEnum.OK.toString());
                    }
                    entityOperation.setOperationLastUpdateDate(Instant.now());
                    this.operationDAO.updateEntity(entityOperation);
                    return Mono.empty();
                });
    }
}
