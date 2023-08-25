package it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.LegalFactListElementDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.ResponsePaperNotificationFailedDtoDto;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.util.List;

public interface PnDeliveryPushClient {

    Flux<ResponsePaperNotificationFailedDtoDto> paperNotificationFailed(String recipientInternalId);
    Flux<LegalFactListElementDto> getNotificationLegalFactsPrivate(String recipientInternalId, String iun);


    }
