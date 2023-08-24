package it.pagopa.pn.service.desk.action;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationDto;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ValidationOperationAction {

    public void validateOperation(String operationId){
        // recupero pnServiceDeskOperation from operationId
        // recupero pnServiceDeskAddress from operationId
            // validationAddress con address manager
        // recupero iun con PnDeliveryPushClient -> paperNotificationFailed
        // update pnServiceDeskOperation status = VALIDATION
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
        return Mono.just(new PnServiceDeskAddress());
    }

    /**
     * Validation address from AddressManagerClient
     * @param address  address's operation
     * @throws it.pagopa.pn.service.desk.exception.PnGenericException only if address is not valid
     */
    private Mono<Void> validationAddress(PnServiceDeskAddress address){
        return Mono.empty();
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

}
