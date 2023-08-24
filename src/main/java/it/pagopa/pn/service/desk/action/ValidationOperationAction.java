package it.pagopa.pn.service.desk.action;

import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ValidationOperationAction {

    public void validateOperation(String operationId){

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



}
