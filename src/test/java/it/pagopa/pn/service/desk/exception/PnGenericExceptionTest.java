package it.pagopa.pn.service.desk.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class PnGenericExceptionTest {

    private final

    @Test
    void getExceptionType() {
    }

    @Test
    void getMessage() {
        assertDoesNotThrow(() -> new PnGenericException(ExceptionTypeEnum.ADDRESS_IS_NOT_PRESENT, "message"));
        assertDoesNotThrow(() -> new PnGenericException(ExceptionTypeEnum.ADDRESS_IS_NOT_PRESENT, "message", HttpStatus.MULTI_STATUS));

    }
}