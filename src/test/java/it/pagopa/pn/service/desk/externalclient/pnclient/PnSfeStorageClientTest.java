package it.pagopa.pn.service.desk.externalclient.pnclient;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.safestorage.PnSafeStorageClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PnSfeStorageClientTest extends BaseTest.WithMockServer{

    @Autowired
    private PnSafeStorageClient pnSafeStorageClient;

    @Test
    void getPresignedUrl(){

    }

    @Test
    void getFile(){

    }
}
