package it.pagopa.pn.service.desk.externalclient.pnclient;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationDto;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PnDeliveryClientTest extends BaseTest.WithMockServer {

    @Autowired
    private PnDeliveryClient pnDeliveryClient;

    @Test
    void getSentNotificationPrivate(){
        SentNotificationDto sentNotificationDto = this.pnDeliveryClient.getSentNotificationPrivate("1234").block();

        Assertions.assertNotNull(sentNotificationDto);

    }
}
