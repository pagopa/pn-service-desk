package it.pagopa.pn.service.desk.exception;


import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ENTITY_NOT_FOUND;

public class PnEntityNotFoundException extends PnGenericException{
    public PnEntityNotFoundException() {
        super(ENTITY_NOT_FOUND, ENTITY_NOT_FOUND.getMessage());
    }
}
