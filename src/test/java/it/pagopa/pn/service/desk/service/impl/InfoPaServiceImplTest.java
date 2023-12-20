package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationSearchResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationSearchRowDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationStatusDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.v1.dto.PaSummaryDto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.PaNotificationsRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.PaSummary;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.SearchNotificationsResponse;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
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

import java.time.OffsetDateTime;
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
    private final NotificationSearchResponseDto expectedNotificationSearchResponse = new NotificationSearchResponseDto();

    @BeforeEach
    public void setUp(){
        expectedPaSummary.setId("123");
        expectedPaSummary.setName("paSummary");

        expectedNotificationSearchResponse.setMoreResult(true);
        expectedNotificationSearchResponse.setNextPagesKey(List.of("eyJlayI6IlBGLWIzY2NhYzMxLTM4ZWEtNDRjZC05NjAxLTlmMmQxOWU4NTNhZiMjMjAyMzA5I"));

        NotificationSearchRowDto notificationSearchRowDto = new NotificationSearchRowDto();
        notificationSearchRowDto.setIun("PRVZ-NZKM-JEDK-202309-A-1");
        notificationSearchRowDto.setPaProtocolNumber("202381856591695996128952");
        notificationSearchRowDto.setSender("Comune di Palermo");
        notificationSearchRowDto.setSentAt(OffsetDateTime.parse("2023-09-29T14:02:08.670718277Z"));
        notificationSearchRowDto.setSubject("Test-di-carico");
        notificationSearchRowDto.setNotificationStatus(NotificationStatusDto.VIEWED);
        notificationSearchRowDto.setRecipients(List.of("GLLGLL64B15G702I"));
        notificationSearchRowDto.setRequestAcceptedAt(OffsetDateTime.parse("2023-09-29T14:03:02.807361187Z"));

        expectedNotificationSearchResponse.setResultsPage(List.of(notificationSearchRowDto));
    }

    @Test
    void getListOfOnboardedPA(){
        Mockito.when(this.externalRegistriesClient.listOnboardedPa())
                .thenReturn(Flux.just(expectedPaSummary));

        PaSummary actual = this.infoPaService.getListOfOnboardedPA(null)
                .blockFirst();

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expectedPaSummary.getId(), actual.getId());
        Assertions.assertEquals(expectedPaSummary.getName(), actual.getName());
    }

    @Test
    void searchNotificationsFromSenderId(){
        Mockito.when(this.pnDeliveryClient.searchNotificationsPrivate(OffsetDateTime.parse("2023-08-31T15:49:05.630Z"),
                        OffsetDateTime.parse("2023-10-10T15:49:05.630Z"), null,
                        "PA-oihdsojn120u", null, null, 50, "nextPageKey"))
                .thenReturn(Mono.just(expectedNotificationSearchResponse));

        SearchNotificationsResponse actual = this.infoPaService.searchNotificationsFromSenderId(null, 50, "nextPageKey", this.getPaNotificationsRequest())
                .block();

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expectedNotificationSearchResponse.getMoreResult(), actual.getMoreResult());
        Assertions.assertEquals(expectedNotificationSearchResponse.getNextPagesKey(), actual.getNextPagesKey());
        Assertions.assertEquals(expectedNotificationSearchResponse.getResultsPage().get(0).getIun(), actual.getResults().get(0).getIun());
        Assertions.assertEquals(expectedNotificationSearchResponse.getResultsPage().get(0).getSender(), actual.getResults().get(0).getSender());
        Assertions.assertEquals(expectedNotificationSearchResponse.getResultsPage().get(0).getSentAt(), actual.getResults().get(0).getSentAt());
        Assertions.assertEquals(expectedNotificationSearchResponse.getResultsPage().get(0).getSubject(), actual.getResults().get(0).getSubject());
        Assertions.assertEquals(expectedNotificationSearchResponse.getResultsPage().get(0).getNotificationStatus().getValue(), actual.getResults().get(0).getIunStatus().getValue());
    }

    private PaNotificationsRequest getPaNotificationsRequest() {
        PaNotificationsRequest paNotificationsRequest = new PaNotificationsRequest();
        paNotificationsRequest.setId("PA-oihdsojn120u");
        paNotificationsRequest.setStartDate(OffsetDateTime.parse("2023-08-31T15:49:05.63Z"));
        paNotificationsRequest.setEndDate(OffsetDateTime.parse("2023-10-10T15:49:05.63Z"));
        return paNotificationsRequest;
    }

}
