package it.pagopa.pn.service.desk.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.Problem;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(JsonMappingException.class)
    public void handle(JsonMappingException e) {
        log.error("Returning HTTP 400 Bad Request {}", e.getMessage());
    }

    @ExceptionHandler(ValueInstantiationException.class)
    public Mono<ResponseEntity<Problem>> handleValueInsantiationException(ValueInstantiationException e) {
        log.warn(e.toString());
        final Problem problem = new Problem();
        settingTraceId(problem);
        problem.setTitle(e.getMessage());
        problem.setDetail(e.getCause().getMessage());
        problem.setStatus(HttpStatus.BAD_REQUEST.value());
        problem.setTimestamp(Instant.now());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem));
    }

    @ExceptionHandler(PnGenericException.class)
    public Mono<ResponseEntity<Problem>> handleResponseEntityException(final PnGenericException exception){
        log.warn(exception.toString());
        final Problem problem = new Problem();
        settingTraceId(problem);
        problem.setTitle(exception.getExceptionType().getTitle());
        problem.setDetail(exception.getMessage());
        problem.setStatus(exception.getHttpStatus().value());
        problem.setTimestamp(Instant.now());
        return Mono.just(ResponseEntity.status(exception.getHttpStatus()).body(problem));
    }




    private void settingTraceId(Problem problem){
        try {
            problem.setTraceId(MDC.get(MDCUtils.MDC_TRACE_ID_KEY));
        } catch (Exception e) {
            log.warn("Cannot get traceid", e);
        }
    }
}
