package it.pagopa.pn.service.desk.filter;

import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.service.desk.exception.PnFilterClientIdException;
import it.pagopa.pn.service.desk.middleware.db.dao.PnClientDAO;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.API_KEY_EMPTY;
import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.API_KEY_NOT_PRESENT;
import static it.pagopa.pn.service.desk.utility.Const.*;

import java.util.List;

@CustomLog
@Component
@AllArgsConstructor
public class ClientIdWebFilter implements WebFilter {
    private PnClientDAO pnClientDAO;

    @Override
    public @NotNull Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        //skip health check status
        if (StringUtils.equals(exchange.getRequest().getPath().toString(), "/")) {
            return chain.filter(exchange);
        }

        HttpHeaders requestHeaders = exchange.getRequest().getHeaders();
        List<String> valuesHeader = requestHeaders.get(HEADER_API_KEY);
        if (valuesHeader == null || valuesHeader.isEmpty()){
            throw new PnFilterClientIdException(API_KEY_EMPTY.getTitle(), API_KEY_EMPTY.getMessage());
        }

        String apiKey = valuesHeader.get(0);
        if (StringUtils.isBlank(apiKey)){
            throw new PnFilterClientIdException(API_KEY_EMPTY.getTitle(), API_KEY_EMPTY.getMessage());
        }

        String ticket = requestHeaders.getFirst(UID_HEADER);
        if (StringUtils.isNotBlank(ticket)){
            MDC.put("cx_type","SD");
            MDC.put("cx_id", ticket.replace("SD-",""));
        }

        Mono<Void> processFilter = pnClientDAO.getByApiKey(apiKey)
            .switchIfEmpty(Mono.error(new PnFilterClientIdException(API_KEY_NOT_PRESENT.getTitle(),
                API_KEY_NOT_PRESENT.getMessage().concat(" ApiKey = ").concat(apiKey)))
            )
            .doOnSuccess(key -> log.info("ApiKey:  {}", key))
            .then(chain.filter(exchange))
            .doFinally(ignored -> log.logEndingProcess("ENDING PROCESS FROM WEB FILTER"));

        return MDCUtils.addMDCToContextAndExecute(processFilter);

    }

}
