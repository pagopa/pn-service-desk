package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.templatesengine;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pntemplatesengine.v1.dto.LanguageEnumDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pntemplatesengine.v1.dto.NotificationCceForEmailDto;
import org.springframework.web.client.RestClientException;
import reactor.core.publisher.Mono;

public interface PnTemplatesEngineClient {


    Mono<String> notificationCceTemplate(LanguageEnumDto xLanguage, NotificationCceForEmailDto request) throws RestClientException;

}
