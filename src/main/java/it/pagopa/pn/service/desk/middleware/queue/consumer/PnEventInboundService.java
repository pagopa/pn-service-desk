package it.pagopa.pn.service.desk.middleware.queue.consumer;

import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.commons.utils.MDCUtils;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.cloud.function.context.MessageRoutingCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.UUID;

import static it.pagopa.pn.service.desk.exception.PnServiceDeskExceptionCodes.*;

@Configuration
@Slf4j
public class PnEventInboundService {
    private final EventHandler eventHandler;
    private final String safeStorageEventQueueName;
    private final String paperChannelEventQueueName;
    private final String externalChannelEventQueueName;

    public PnEventInboundService(EventHandler eventHandler, PnServiceDeskConfigs cfg) {
        this.eventHandler = eventHandler;
        this.safeStorageEventQueueName = cfg.getTopics().getSafeStorageEvents();
        this.paperChannelEventQueueName = cfg.getTopics().getPaperChannelQueue();
        this.externalChannelEventQueueName = cfg.getTopics().getExternalChannelQueue();
    }

    @Bean
    public MessageRoutingCallback customRouter() {
        return new MessageRoutingCallback() {
            @Override
            public FunctionRoutingResult routingResult(Message<?> message) {
                setMdc(message);
                return new FunctionRoutingResult(handleMessage(message));
            }
        };
    }

    private void setMdc(Message<?> message) {
        MessageHeaders messageHeaders = message.getHeaders();
        MDCUtils.clearMDCKeys();
        
        if (messageHeaders.containsKey("aws_messageId")){
            String awsMessageId = messageHeaders.get("aws_messageId", String.class);
            MDC.put(MDCUtils.MDC_PN_CTX_MESSAGE_ID, awsMessageId);
        }
        
        if (messageHeaders.containsKey("X-Amzn-Trace-Id")){
            String traceId = messageHeaders.get("X-Amzn-Trace-Id", String.class);
            MDC.put(MDCUtils.MDC_TRACE_ID_KEY, traceId);
        } else {
            MDC.put(MDCUtils.MDC_TRACE_ID_KEY, String.valueOf(UUID.randomUUID()));
        }

        String iun = (String) message.getHeaders().get("iun");
        if(iun != null){
            MDC.put(MDCUtils.MDC_PN_IUN_KEY, iun);
        }
    }

    private String handleMessage(Message<?> message) {
        String eventType = (String) message.getHeaders().get("eventType");
        log.debug("Received message from customRouter with eventType={}", eventType);

        eventType = getEventType(message);

        String handlerName = eventHandler.getHandler().get(eventType);
        if (!StringUtils.hasText(handlerName)) {
            log.error("undefined handler for eventType={}", eventType);
        }

        log.debug("Handler for eventType={} is {}", eventType, handlerName);

        return handlerName;
    }

    @NotNull
    private String getEventType(Message<?> message) {
        String eventType = (String) message.getHeaders().get("eventType");

        if(eventType != null) {
            return eventType;
        }
        else {
            String queueName = (String) message.getHeaders().get("aws_receivedQueue");

            if (Objects.equals(queueName, safeStorageEventQueueName)) {
                return  "SAFE_STORAGE_EVENTS";
            }
            if (Objects.equals(queueName, paperChannelEventQueueName)){
                return "PAPER_CHANNEL_EVENTS";
            }
            if (Objects.equals(queueName, externalChannelEventQueueName)){
                return "EXTERNAL_CHANNEL_EVENTS";
            }
            else {
                log.error("eventType not present, cannot start scheduled action headers={} payload={}", message.getHeaders(), message.getPayload());
                throw new PnInternalException("eventType not present, cannot start scheduled action", ERROR_CODE_SERVICEDESK_EVENTTYPENOTSUPPORTED);
            }
        }
    }


}
