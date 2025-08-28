package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.templatesengine;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pntemplatesengine.v1.api.TemplateApi;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pntemplatesengine.v1.dto.LanguageEnumDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pntemplatesengine.v1.dto.NotificationCceForEmailDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class PnTemplatesEngineClientImpl implements PnTemplatesEngineClient {

    private TemplateApi templateApi;

    @Override
    public Mono<String> notificationCceTemplate(LanguageEnumDto xLanguage, NotificationCceForEmailDto request) throws RestClientException {
        return templateApi.notificationCceForEmail(xLanguage, request);
    }
}
