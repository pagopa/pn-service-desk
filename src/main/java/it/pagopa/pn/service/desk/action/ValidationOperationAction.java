package it.pagopa.pn.service.desk.action;

import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationDto;
import it.pagopa.pn.service.desk.middleware.db.dao.AddressDAO;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.addressmanager.PnAddressManagerClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClient;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import it.pagopa.pn.service.desk.utility.Utility;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ADDRESS_IS_NOT_VALID;

@Component
public class ValidationOperationAction {

    @Autowired
    private OperationDAO operationDAO;

    @Autowired
    private AddressDAO addressDAO;

    @Autowired
    private PnAddressManagerClient addressManagerClient;

    @Autowired
    private PnDeliveryPushClient pnDeliveryPushClient;

    public void validateOperation(String operationId){
        operationDAO.getByOperationId(operationId)
                .zipWhen(operations ->
                        getAddressFromOperationId(operationId)
                ).map(operationAndAddress ->
                    pnDeliveryPushClient.paperNotificationFailed(operationAndAddress.getT1().getOperationId())
                            .collectList().doOnSuccess(responsePaperNotificationFailed -> updateStatus(operationAndAddress.getT1(), OperationStatusEnum.VALIDATION))
                            .flatMapMany(Flux::fromIterable)
                            .parallel()
                            .flatMap(notificationFailed -> getNotificationsAttachments(notificationFailed.getIun()))
                            .sequential()
                            .flatMap(pnServiceDeskAttachments -> Flux.fromIterable(pnServiceDeskAttachments.getFilesKey()))
                            .collectList()
                            .flatMap(attachments -> paperPrepare(operationAndAddress.getT1(), operationAndAddress.getT2(), attachments))
                );

            // per ogni iun recuperato da deliveryPush ->
            // getNotificationsAttachments(iun);
        // update pnServiceDeskOperation aggiungendo la lista dei PnServiceDeskAttachments
        //Utility.GenerateRequestId(operationId)
        // chiamare la prepare di PaperChannel -> PnPaperChannelClient.prepare()
        // se chiamata di prepare ritorna 201 -> update pnServiceDeskOperation con status = PREPARING
    }

    /**
     * Retrieve address from AddressDAO
     * @param operationId id of operation
     * @return Address from DB
     */
    private Mono<PnServiceDeskAddress> getAddressFromOperationId(String operationId){
        return addressDAO.getAddress(operationId)
                .doOnNext(this::validationAddress);
    }

    /**
     * Validation address from AddressManagerClient
     * @param address  address's operation
     * @throws it.pagopa.pn.service.desk.exception.PnGenericException only if address is not valid
     */
    private Mono<Void> validationAddress(PnServiceDeskAddress address){
        return addressManagerClient.deduplicates(address)
                .flatMap(deduplicateResponse -> {
                    if (Boolean.FALSE.equals(deduplicateResponse.getEqualityResult())) {
                        return Mono.error(new PnGenericException(ADDRESS_IS_NOT_VALID, ADDRESS_IS_NOT_VALID.getMessage()));
                    }
                    if (StringUtils.isNotBlank(deduplicateResponse.getError())){
                        return Mono.error(new PnGenericException(ADDRESS_IS_NOT_VALID, ADDRESS_IS_NOT_VALID.getMessage()));
                    }
                    return Mono.just(deduplicateResponse);
                }).then();
    }





    private Mono<PnServiceDeskAttachments> getNotificationsAttachments(String iun){
        //TODO chiamata deliveryPush legalfact
        //TODO chiamata delivery getNotification
        // estrarre lista di FileKey = List<String>
        return Mono.empty();
    }

    private Flux<String> getAttachmentsFromDeliveryPush(/*LegalFacts*/){
        //TODO retrive only fileKey
        return Flux.empty();
    }

    private Flux<String> getAttachmentsFromDelivery(SentNotificationDto sentNotificationDto){
        //TODO retrive only fileKey
        //sentNotificationDto.getDocuments().get(0).getRef().getKey(); da iterare
        return Flux.empty();
    }

    private Mono<PnServiceDeskOperations> updateStatus (PnServiceDeskOperations operations, OperationStatusEnum operationStatusEnum){
        operations.setStatus(operationStatusEnum.toString());
        return this.operationDAO.updateEntity(operations);
    }

    private Mono<Void> paperPrepare (PnServiceDeskOperations operations, PnServiceDeskAddress address, List<String> attachments){
        String requestId = Utility.generateRequestId(operations.getOperationId());
//        TODO mappare tutti i parametri nella prepareRequest, passare al client di paper
        return null;
    }

}
