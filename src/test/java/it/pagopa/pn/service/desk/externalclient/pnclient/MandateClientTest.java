package it.pagopa.pn.service.desk.externalclient.pnclient;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnmandate.v1.dto.InternalMandateDtoDto;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.mandate.MandateClientImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class MandateClientTest extends BaseTest.WithMockServer{

    @Autowired
    private MandateClientImpl mandateClient;

    @Test
    void listMandatesByDelegator(){
        List<InternalMandateDtoDto> response = mandateClient.listMandatesByDelegator("PF-4fc75df3-0913-407e-bdaa-e50329708b7d")
                .collectList()
                .block();

        Assertions.assertNotNull(response);

        InternalMandateDtoDto firstMandate = response.get(0);
        InternalMandateDtoDto secondMandate = response.get(1);

        Assertions.assertEquals("49258827-a23d-4712-a46f-e23a67b4150f", firstMandate.getMandateId());
        Assertions.assertEquals("PF-4fc75df3-0913-407e-bdaa-e50329708b7d", firstMandate.getDelegator());
        Assertions.assertEquals("PF-37654561-446a-4c88-b328-6699a8322b33", firstMandate.getDelegate());
        Assertions.assertEquals("026e8c72-7944-4dcd-8668-f596447fec6d", firstMandate.getVisibilityIds().get(0));
        Assertions.assertEquals("2023-06-18T22:00:00Z", firstMandate.getDatefrom());
        Assertions.assertEquals("2023-10-18T21:59:59Z", firstMandate.getDateto());

        Assertions.assertEquals("64270e52-b5d5-4cf3-bbe2-15e8ac750107", secondMandate.getMandateId());
        Assertions.assertEquals("PF-4fc75df3-0913-407e-bdaa-e50329708b7d", secondMandate.getDelegator());
        Assertions.assertEquals("PG-8f409ca6-fc3d-46c3-8da7-69cd9a1e9e62", secondMandate.getDelegate());
        Assertions.assertEquals("026e8c72-7944-4dcd-8668-f596447fec6d", secondMandate.getVisibilityIds().get(0));
        Assertions.assertEquals("2023-01-10T23:00:00Z", secondMandate.getDatefrom());
        Assertions.assertEquals("2026-05-12T21:59:59Z", secondMandate.getDateto());
        Assertions.assertEquals("645cf38b2030541ce50153b5", secondMandate.getGroups().get(0));
    }

    @Test
    void listMandatesByDelegate(){
        List<InternalMandateDtoDto> response = mandateClient.listMandatesByDelegate("PF-4fc75df3-0913-407e-bdaa-e50329708b7d")
                .collectList()
                .block();

        Assertions.assertNotNull(response);

        InternalMandateDtoDto firstMandate = response.get(0);
        InternalMandateDtoDto secondMandate = response.get(1);

        Assertions.assertEquals("49258827-a23d-4712-a46f-e23a67b4150f", firstMandate.getMandateId());
        Assertions.assertEquals("PF-4fc75df3-0913-407e-bdaa-e50329708b7d", firstMandate.getDelegator());
        Assertions.assertEquals("PF-37654561-446a-4c88-b328-6699a8322b33", firstMandate.getDelegate());
        Assertions.assertEquals("026e8c72-7944-4dcd-8668-f596447fec6d", firstMandate.getVisibilityIds().get(0));
        Assertions.assertEquals("2023-06-18T22:00:00Z", firstMandate.getDatefrom());
        Assertions.assertEquals("2023-10-18T21:59:59Z", firstMandate.getDateto());

        Assertions.assertEquals("64270e52-b5d5-4cf3-bbe2-15e8ac750107", secondMandate.getMandateId());
        Assertions.assertEquals("PF-4fc75df3-0913-407e-bdaa-e50329708b7d", secondMandate.getDelegator());
        Assertions.assertEquals("PG-8f409ca6-fc3d-46c3-8da7-69cd9a1e9e62", secondMandate.getDelegate());
        Assertions.assertEquals("026e8c72-7944-4dcd-8668-f596447fec6d", secondMandate.getVisibilityIds().get(0));
        Assertions.assertEquals("2023-01-10T23:00:00Z", secondMandate.getDatefrom());
        Assertions.assertEquals("2026-05-12T21:59:59Z", secondMandate.getDateto());
        Assertions.assertEquals("645cf38b2030541ce50153b5", secondMandate.getGroups().get(0));
    }

}