package it.pagopa.pn.service.desk.action.impl;

import it.pagopa.pn.service.desk.action.NotifyDeliveryPushAction;
import it.pagopa.pn.service.desk.action.common.CommonAction;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.mapper.OperationMapper;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClient;
import it.pagopa.pn.service.desk.middleware.queue.model.InternalEventBody;
import it.pagopa.pn.service.desk.middleware.queue.producer.InternalQueueMomProducer;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.OPERATION_IS_NOT_PRESENT;

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
            operationDAO.getByOperationId(internalEventBody.getOperationId())
                    .switchIfEmpty(Mono.error(new PnGenericException(OPERATION_IS_NOT_PRESENT, OPERATION_IS_NOT_PRESENT.getMessage(), HttpStatus.NOT_FOUND)))
                    .flatMap(operations -> operationDAO.updateEntity(OperationMapper.updateOperations(operations, OperationStatusEnum.OK)));
        }

        if (pnServiceDeskConfigs.getNotifyAttempt() > internalEventBody.getAttempt()) {
            log.info("call notifyNotificationViewed with iun {}", internalEventBody.getIuns().get(0));
            Mono.just("").publishOn(Schedulers.boundedElastic())
                    .flatMap(y -> pnDeliveryPushClient.notifyNotificationViewed(internalEventBody.getIuns().get(0), internalEventBody.getOperationId(), internalEventBody.getRecipientInternalId())
                            .switchIfEmpty(Mono.defer(() -> {
                                log.info("an error occurs in notifyNotificationViewed with iun", internalEventBody.getIuns().get(0));
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
            operationDAO.getByOperationId(internalEventBody.getOperationId())
                    .switchIfEmpty(Mono.error(new PnGenericException(OPERATION_IS_NOT_PRESENT, OPERATION_IS_NOT_PRESENT.getMessage(), HttpStatus.NOT_FOUND)))
                    .flatMap(operations -> operationDAO.updateEntity(OperationMapper.updateOperations(operations, OperationStatusEnum.NOTIFY_VIEW_ERROR)));
        }
    }


}
