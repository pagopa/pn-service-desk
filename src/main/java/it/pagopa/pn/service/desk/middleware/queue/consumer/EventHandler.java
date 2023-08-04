package it.pagopa.pn.service.desk.middleware.queue.consumer;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "pn.service-desk.event")
public class EventHandler {
    private Map<String, String> handler;
}
