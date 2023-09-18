package it.pagopa.pn.service.desk.exception;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.API_KEY_EMPTY;
import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ENTITY_NOT_FOUND;

@Getter
public class PnFilterClientIdException extends PnRuntimeException {

    public PnFilterClientIdException(String message, String description){
        super(message, description, HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.toString(), message, description);
    }
}
