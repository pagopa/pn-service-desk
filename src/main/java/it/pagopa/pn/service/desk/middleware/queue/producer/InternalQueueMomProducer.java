package it.pagopa.pn.service.desk.middleware.queue.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.api.dto.events.AbstractSqsMomProducer;
import it.pagopa.pn.service.desk.middleware.queue.model.InternalEvent;
import software.amazon.awssdk.services.sqs.SqsClient;

public class InternalQueueMomProducer extends AbstractSqsMomProducer<InternalEvent>{


    public InternalQueueMomProducer(SqsClient sqsClient, String topic, ObjectMapper objectMapper, Class<InternalEvent> msgClass) {
        super(sqsClient, topic, objectMapper, msgClass);
    }


}
