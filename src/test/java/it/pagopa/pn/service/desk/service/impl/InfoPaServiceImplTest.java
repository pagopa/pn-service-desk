package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.LegalNotificationSearchResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.LegalNotificationSearchRowDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationStatusV26Dto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.v1.dto.PaSummaryDto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.PaNotificationsRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.PaSummary;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.SearchNotificationsResponse;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery.PnDeliveryClient.SearchNotificationsPrivateHeaders;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery.PnDeliveryClient.SearchNotificationsPrivateParameters;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.externalregistries.ExternalRegistriesClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
@ExtendWith(MockitoExtension.class)
class InfoPaServiceImplTest {

    @Mock
    private ExternalRegistriesClient externalRegistriesClient;
    @Mock
    private PnDeliveryClient pnDeliveryClient;
    @InjectMocks
    private InfoPaServiceImpl infoPaService;
    @Spy
    private AuditLogServiceImpl auditLogService;

    private final PaSummaryDto expectedPaSummary = new PaSummaryDto();
    private final LegalNotificationSearchResponseDto expectedNotificationSearchResponse = new LegalNotificationSearchResponseDto();

    @BeforeEach
    public void setUp(){
        expectedPaSummary.setId("123");
        expectedPaSummary.setName("paSummary");

        expectedNotificationSearchResponse.setMoreResult(true);
        expectedNotificationSearchResponse.setNextPagesKey(List.of("eyJlayI6IlBGLWIzY2NhYzMxLTM4ZWEtNDRjZC05NjAxLTlmMmQxOWU4NTNhZiMjMjAyMzA5I"));

        LegalNotificationSearchRowDto legalNotificationSearchRowDto = new LegalNotificationSearchRowDto();
        legalNotificationSearchRowDto.setIun("PRVZ-NZKM-JEDK-202309-A-1");
        legalNotificationSearchRowDto.setPaProtocolNumber("202381856591695996128952");
        legalNotificationSearchRowDto.setSender("Comune di Palermo");
        legalNotificationSearchRowDto.setSentAt(Instant.parse("2023-09-29T14:02:08.670718277Z"));
        legalNotificationSearchRowDto.setSubject("Test-di-carico");
        legalNotificationSearchRowDto.setNotificationStatus(NotificationStatusV26Dto.VIEWED);
        legalNotificationSearchRowDto.setRecipients(List.of("GLLGLL64B15G702I"));
        legalNotificationSearchRowDto.setRequestAcceptedAt(Instant.parse("2023-09-29T14:03:02.807361187Z"));

        expectedNotificationSearchResponse.setResultsPage(List.of(legalNotificationSearchRowDto));
    }

    @Test
    void getListOfOnboardedPA(){
        Mockito.when(this.externalRegistriesClient.listOnboardedPa(null))
                .thenReturn(Flux.just(expectedPaSummary));

        PaSummary actual = this.infoPaService.getListOfOnboardedPA(null, null)
                .blockFirst();

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expectedPaSummary.getId(), actual.getId());
        Assertions.assertEquals(expectedPaSummary.getName(), actual.getName());
    }

    @Test
    void searchNotificationsFromSenderId(){
        Mockito.when(this.pnDeliveryClient.searchNotificationsPrivate(
                        new SearchNotificationsPrivateHeaders("operator-uid", null),
                        new SearchNotificationsPrivateParameters(Instant.parse("2023-08-31T15:49:05.630Z"),
                                Instant.parse("2023-10-10T15:49:05.630Z"), null, "PA-oihdsojn120u",
                                null, null, 50, "nextPageKey")))
                .thenReturn(Mono.just(expectedNotificationSearchResponse));

        SearchNotificationsResponse actual = this.infoPaService.searchNotificationsFromSenderId("operator-uid", null, 50, "nextPageKey", this.getPaNotificationsRequest())
                .block();

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expectedNotificationSearchResponse.getMoreResult(), actual.getMoreResult());
        Assertions.assertEquals(expectedNotificationSearchResponse.getNextPagesKey(), actual.getNextPagesKey());
        Assertions.assertNotNull(expectedNotificationSearchResponse.getResultsPage());
        Assertions.assertEquals(expectedNotificationSearchResponse.getResultsPage().getFirst().getIun(), actual.getResults().getFirst().getIun());
        Assertions.assertEquals(expectedNotificationSearchResponse.getResultsPage().getFirst().getSender(), actual.getResults().getFirst().getSender());
        Assertions.assertEquals(expectedNotificationSearchResponse.getResultsPage().getFirst().getSentAt(), actual.getResults().getFirst().getSentAt());
        Assertions.assertEquals(expectedNotificationSearchResponse.getResultsPage().getFirst().getSubject(), actual.getResults().getFirst().getSubject());
        Assertions.assertNotNull(expectedNotificationSearchResponse.getResultsPage().getFirst().getNotificationStatus());
        Assertions.assertEquals(expectedNotificationSearchResponse.getResultsPage().getFirst().getNotificationStatus().getValue(), actual.getResults().getFirst().getIunStatus().getValue());
    }

    private PaNotificationsRequest getPaNotificationsRequest() {
        PaNotificationsRequest paNotificationsRequest = new PaNotificationsRequest();
        paNotificationsRequest.setId("PA-oihdsojn120u");
        paNotificationsRequest.setStartDate(Instant.parse("2023-08-31T15:49:05.63Z"));
        paNotificationsRequest.setEndDate(Instant.parse("2023-10-10T15:49:05.63Z"));
        return paNotificationsRequest;
    }

}
