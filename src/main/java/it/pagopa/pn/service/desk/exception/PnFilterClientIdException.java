package it.pagopa.pn.service.desk.exception;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PnFilterClientIdException extends PnRuntimeException {

    public PnFilterClientIdException(String message, String description){
        super(message, description, HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.toString(), message, description);
    }
}
