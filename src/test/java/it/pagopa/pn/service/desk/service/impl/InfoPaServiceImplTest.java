package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationSearchResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationSearchRowDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationStatusV26Dto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.v1.dto.PaSummaryDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.v1.dto.PaSummaryExtendedDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalregistries.v1.dto.PaSummaryExtendedResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
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
import java.time.Instant;
import java.util.ArrayList;
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
    private final PaSummaryExtendedResponseDto paSummaryExtendedResponseDto = new PaSummaryExtendedResponseDto();


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
        notificationSearchRowDto.setSentAt(Instant.parse("2023-09-29T14:02:08.670718277Z"));
        notificationSearchRowDto.setSubject("Test-di-carico");
        notificationSearchRowDto.setNotificationStatus(NotificationStatusV26Dto.VIEWED);
        notificationSearchRowDto.setRecipients(List.of("GLLGLL64B15G702I"));
        notificationSearchRowDto.setRequestAcceptedAt(Instant.parse("2023-09-29T14:03:02.807361187Z"));

        expectedNotificationSearchResponse.setResultsPage(List.of(notificationSearchRowDto));

        List<PaSummaryExtendedDto> paSummaryExtendedDtoList = new ArrayList<>();
        PaSummaryExtendedDto paSummaryExtendedDto = new PaSummaryExtendedDto();
        paSummaryExtendedDto.setId("0");
        paSummaryExtendedDto.setName("Comune di Firenze");
        paSummaryExtendedDtoList.add(paSummaryExtendedDto);
        paSummaryExtendedResponseDto.setContent(paSummaryExtendedDtoList);
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
    void getExtendedListOfOnboardedPA(){
        // Given & When
        Mockito.when(this.externalRegistriesClient.extendedListOnboardedPa("Comune", false, 1, 10))
                .thenReturn(Mono.just(paSummaryExtendedResponseDto));

        // Then
        PaSummaryExtendedResponse paSummaryExtendedResponse = this.infoPaService.getExtendedListOfOnboardedPA(null, "Comune", false, 1, 10)
                .block();

        Assertions.assertNotNull(paSummaryExtendedResponse);
        Assertions.assertEquals(paSummaryExtendedResponseDto.getContent().get(0).getId(), paSummaryExtendedResponse.getContent().get(0).getId());
        Assertions.assertEquals(paSummaryExtendedResponseDto.getContent().get(0).getName(), paSummaryExtendedResponse.getContent().get(0).getName());
    }

    @Test
    void getExtendedListOfOnboardedPA_OnlyChildren() {
        // When
        Mockito.when(this.externalRegistriesClient.extendedListOnboardedPa("Comune", true, 1, 10))
                .thenReturn(Mono.just(paSummaryExtendedResponseDto));

        // Then
        PaSummaryExtendedResponse paSummaryExtendedResponse = this.infoPaService.getExtendedListOfOnboardedPA(null, "Comune", true, 1, 10)
                .block();

        Assertions.assertNotNull(paSummaryExtendedResponse);
        Assertions.assertFalse(paSummaryExtendedResponse.getContent().isEmpty());
    }

    @Test
    void getExtendedListOfOnboardedPA_Pagination() {
        // Given
        Integer page = 2;
        Integer size = 5;

        Mockito.when(this.externalRegistriesClient.extendedListOnboardedPa("Comune", false, page, size))
                .thenReturn(Mono.just(paSummaryExtendedResponseDto));

        // When
        PaSummaryExtendedResponse paSummaryExtendedResponse = this.infoPaService.getExtendedListOfOnboardedPA(null, "Comune", false, page, size)
                .block();

        // Then
        Assertions.assertNotNull(paSummaryExtendedResponse);
        Assertions.assertFalse(paSummaryExtendedResponse.getContent().isEmpty());
    }

    @Test
    void searchNotificationsFromSenderId(){
        Mockito.when(this.pnDeliveryClient.searchNotificationsPrivate(Instant.parse("2023-08-31T15:49:05.630Z"),
                        Instant.parse("2023-10-10T15:49:05.630Z"), null,
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
        paNotificationsRequest.setStartDate(Instant.parse("2023-08-31T15:49:05.63Z"));
        paNotificationsRequest.setEndDate(Instant.parse("2023-10-10T15:49:05.63Z"));
        return paNotificationsRequest;
    }

}
