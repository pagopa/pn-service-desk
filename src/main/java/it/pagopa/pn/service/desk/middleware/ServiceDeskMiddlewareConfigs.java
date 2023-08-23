package it.pagopa.pn.service.desk.middleware;


import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.middleware.queue.model.InternalEvent;
import it.pagopa.pn.service.desk.middleware.queue.producer.InternalQueueMomProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@Slf4j
public class ServiceDeskMiddlewareConfigs {

    @Bean
    public InternalQueueMomProducer getInternalQueueMomProducer(SqsClient sqsClient, ObjectMapper mapper, PnServiceDeskConfigs pnServiceDeskConfigs) {
        return new InternalQueueMomProducer(sqsClient, pnServiceDeskConfigs.getTopics().getInternalQueue(), mapper, InternalEvent.class);
    }

}
