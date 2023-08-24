package it.pagopa.pn.service.desk.middleware.queue.model;

import it.pagopa.pn.api.dto.events.GenericEvent;
import it.pagopa.pn.api.dto.events.GenericEventHeader;
import lombok.AllArgsConstructor;


@AllArgsConstructor
public class InternalEvent implements GenericEvent<GenericEventHeader, InternalEventBody> {

    private GenericEventHeader header;
    private InternalEventBody payload;


    @Override
    public GenericEventHeader getHeader() {
        return this.header;
    }

    @Override
    public InternalEventBody getPayload() {
        return this.payload;
    }
}
