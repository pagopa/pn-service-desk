package it.pagopa.pn.service.desk.exception;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import lombok.Getter;

@Getter
public class PnFilterClientIdException extends PnRuntimeException {

    private final String description;

    public PnFilterClientIdException(String message, String description, int status, String errorCode){
        super(message, description, status, errorCode, null, null);
        this.description = description;
    }
}
