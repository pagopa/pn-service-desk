package it.pagopa.pn.service.desk.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.Problem;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Slf4j
@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(JsonMappingException.class)
    public void handle(JsonMappingException e) {
        log.error("Returning HTTP 400 Bad Request {}", e.getMessage());
    }

    @ExceptionHandler(PnGenericException.class)
    public Mono<ResponseEntity<Problem>> handleResponseEntityException(final PnGenericException exception){
        log.warn(exception.toString());
        final Problem problem = new Problem();
        settingTraceId(problem);
        problem.setTitle(exception.getExceptionType().getTitle());
        problem.setDetail(exception.getMessage());
        problem.setStatus(exception.getHttpStatus().value());
        problem.setTimestamp(OffsetDateTime.now());
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
