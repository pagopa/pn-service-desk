package it.pagopa.pn.service.desk.action.impl;

import it.pagopa.pn.service.desk.action.NotifyDeliveryPushAction;
import it.pagopa.pn.service.desk.action.common.CommonAction;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClient;
import it.pagopa.pn.service.desk.middleware.queue.model.InternalEventBody;
import it.pagopa.pn.service.desk.middleware.queue.producer.InternalQueueMomProducer;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.Collectors;

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
        log.info("NotifyDeliveryPushActionImpl execute attempt nro {}", internalEventBody.getAttempt());
        if (internalEventBody.getIuns() == null || internalEventBody.getIuns().isEmpty()) {
            operationDAO.updateEntity(null); // TODO aggiorno lo stato in OK
        }

        if (pnServiceDeskConfigs.getNotifyAttempt() > internalEventBody.getAttempt()) {
            log.info("notifyNotificationViewed with iun {}", internalEventBody.getIuns().get(0));
            Mono.just("").publishOn(Schedulers.boundedElastic())
                    .flatMap(y -> pnDeliveryPushClient.notifyNotificationViewed(internalEventBody.getIuns().get(0), internalEventBody.getOperationId(), internalEventBody.getRecipientInternalId())
                            .switchIfEmpty(Mono.defer(() -> {
                                log.info("push message on queue with iuns size: {}", internalEventBody.getIuns().size());
                                internalQueueMomProducer.push
                                        (getInternalEvent(internalEventBody.getIuns(), internalEventBody.getOperationId(), internalEventBody.getRecipientInternalId(), internalEventBody.getAttempt()+1));
                                return Mono.empty();
                            }))
                            .flatMap(a -> {
                                List<String> newIuns = internalEventBody.getIuns().subList(1, internalEventBody.getIuns().size());
                                log.info("push message on queue with iuns size: {}", newIuns.size());
                                internalQueueMomProducer.push(getInternalEvent(newIuns, internalEventBody.getOperationId(), internalEventBody.getRecipientInternalId()));
                                return Mono.empty();
                            }))
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe();
        } else {
            log.warn("attempt has finished for operationId {}", internalEventBody.getOperationId());
        }
    }


}
