package it.pagopa.pn.service.desk.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;


@Getter
public class PnGenericException extends RuntimeException {
    private final ExceptionTypeEnum exceptionType;
    private final HttpStatusCode httpStatus;
    private final String message;


    public PnGenericException(ExceptionTypeEnum exceptionType, String message){
        super(message);
        this.exceptionType = exceptionType;
        this.message = message;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    public PnGenericException(ExceptionTypeEnum exceptionType, HttpStatusCode httpStatus) {
        this.exceptionType = exceptionType;
        this.httpStatus = httpStatus;
        this.message = "See logs for details in PN-SERVICE-DESK";
    }

    public PnGenericException(ExceptionTypeEnum exceptionType, String message, HttpStatusCode status){
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
