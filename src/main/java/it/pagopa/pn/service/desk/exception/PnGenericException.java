package it.pagopa.pn.service.desk.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public class PnGenericException extends RuntimeException {
    private final ExceptionTypeEnum exceptionType;
    private final HttpStatus httpStatus;
    private final String message;


        public PnGenericException(ExceptionTypeEnum exceptionType, String message){
        super(message);
        this.exceptionType = exceptionType;
        this.message = message;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    public PnGenericException(ExceptionTypeEnum exceptionType, String message, HttpStatus status){
        super(message);
        this.exceptionType = exceptionType;
        this.message = message;
        this.httpStatus = status;
    }

    public PnGenericException(ExceptionTypeEnum exceptionType, String message, Throwable throwable){
        super(message, throwable);
        this.exceptionType = exceptionType;
        this.message = message;
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }

}
