package it.pagopa.pn.service.desk.middleware.queue.model;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InternalEventBody {
    private String operationId;
    private List<String> iuns;
    private String recipientInternalId;
    private int attempt;
}
