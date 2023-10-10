package it.pagopa.pn.service.desk.service;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.PaSummary;
import reactor.core.publisher.Flux;

public interface InfoPaService {

    Flux<PaSummary> getListOfOnboardedPA(String xPagopaPnUid);

}
