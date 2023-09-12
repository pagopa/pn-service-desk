package it.pagopa.pn.service.desk.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PnRetryStorageExceptionTest {

    @Test
    void getRetryAfter() {
        assertDoesNotThrow(() -> new PnRetryStorageException(new BigDecimal(1)));
    }
}