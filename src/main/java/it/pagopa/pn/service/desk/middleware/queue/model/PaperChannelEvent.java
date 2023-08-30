package it.pagopa.pn.service.desk.middleware.queue.model;

import it.pagopa.pn.api.dto.events.GenericEvent;
import it.pagopa.pn.api.dto.events.GenericEventHeader;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.PaperChannelUpdateDto;

public class PaperChannelEvent implements GenericEvent<GenericEventHeader, PaperChannelUpdateDto>  {
    private GenericEventHeader header;
    private PaperChannelUpdateDto payload;

    @Override
    public GenericEventHeader getHeader() {
        return header;
    }

    @Override
    public PaperChannelUpdateDto getPayload() {
        return payload;
    }
}
