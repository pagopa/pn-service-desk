package it.pagopa.pn.service.desk.externalclient.pnclient;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PnDeliveryPushClientTest extends BaseTest.WithMockServer{

    @Autowired
    private PnDeliveryPushClient pnDeliveryPushClient;

    @Test
    void paperNotificationFailed(){

    }

    @Test
    void getNotificationLegalFactsPrivate(){

    }
}
