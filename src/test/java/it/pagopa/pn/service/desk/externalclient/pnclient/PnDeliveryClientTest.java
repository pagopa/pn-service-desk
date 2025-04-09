package it.pagopa.pn.service.desk.externalclient.pnclient;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.*;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


class PnDeliveryClientTest extends BaseTest.WithMockServer {

    @Autowired
    private PnDeliveryClient pnDeliveryClient;
    
    private final SentNotificationV25Dto expectedNotification = new SentNotificationV25Dto();
    private final NotificationSearchResponseDto expectedNotificationSearchResponse = new NotificationSearchResponseDto();
    private final NotificationAttachmentDownloadMetadataResponseDto expectedNotificationAttachment = new NotificationAttachmentDownloadMetadataResponseDto();

    @BeforeEach
    public void setUp(){
        expectedNotification.setIdempotenceToken("idenpotence123");
        expectedNotification.setPaProtocolNumber("paProtocolNumber1234");
        expectedNotification.setSubject("Subject432");
        expectedNotification.setAbstract("abstract121");
        expectedNotification.setRecipients(new ArrayList<>());
        
        List<NotificationDocumentDto> notificationDocumentDtoList = new ArrayList<>();
        notificationDocumentDtoList.add(
                this.getNotificationDocument(
                "ContentType",
                "1234567",
                "versionToken6",
                "title34",
                "docIdx98"
                )
        );
        notificationDocumentDtoList.add(
                this.getNotificationDocument(
                        "ContentType",
                        "7654321",
                        "versionToken1",
                        "title34",
                        "docIdx98"
                )
        );
        notificationDocumentDtoList.add(
                this.getNotificationDocument(
                        "ContentType",
                        "981234",
                        "versionToken2",
                        "title34",
                        "docIdx98"
                )
        );
        expectedNotification.setDocuments(notificationDocumentDtoList);
        expectedNotification.setCancelledIun("cancelledIun354");

        expectedNotificationSearchResponse.setMoreResult(true);
        expectedNotificationSearchResponse.setNextPagesKey(List.of("eyJlayI6IlBGLWIzY2NhYzMxLTM4ZWEtNDRjZC05NjAxLTlmMmQxOWU4NTNhZiMjMjAyMzA5IiwiaWsiOnsiaXVuX3JlY2lwaWV" +
                "udElkIjoiWllXVC1HQVhELVhVR1YtMjAyMzA5LUgtMSMjUEYtYjNjY2FjMzEtMzhlYS00NGNkLTk2MDEtOWYyZDE5ZTg1M2FmIiwicmVjaXBpZW50SWRfY3JlYXRpb25Nb250aCI6IlBG" +
                "LWIzY2NhYzMxLTM4ZWEtNDRjZC05NjAxLTlmMmQxOWU4NTNhZiMjMjAyMzA5Iiwic2VudEF0IjoiMjAyMy0wOS0yOVQxNDowMjowNC4yNTU1NTQwODBaIn19"));

        List<NotificationSearchRowDto> notificationSearchRowDtoList = new ArrayList<>();
        notificationSearchRowDtoList.add(this.getNotificationSearchRow(
                "PRVZ-NZKM-JEDK-202309-A-1",
                "202381856591695996128952",
                "Comune di Palermo",
                Instant.parse("2023-09-29T14:02:08.670718277Z"),
                "Test-di-carico",
                NotificationStatusV26Dto.VIEWED,
                List.of("GLLGLL64B15G702I"),
                Instant.parse("2023-09-29T14:03:02.807361187Z")
        ));
        notificationSearchRowDtoList.add(this.getNotificationSearchRow(
                "ENEZ-VXZU-JDJQ-202309-L-1",
                "202380813111695996128483",
                "Comune di Palermo",
                Instant.parse("2023-09-29T14:02:08.203039228Z"),
                "Test-di-carico",
                NotificationStatusV26Dto.VIEWED,
                List.of("GLLGLL64B15G702I"),
                Instant.parse("2023-09-29T14:03:10.91919327Z")
        ));
        expectedNotificationSearchResponse.setResultsPage(notificationSearchRowDtoList);

        expectedNotificationAttachment.setFilename("PRVZ-NZKM-JEDK-202309-A-1__Test_PDF.pdf");
        expectedNotificationAttachment.setContentType("application/pdf");
        expectedNotificationAttachment.setContentLength(10431);
        expectedNotificationAttachment.setSha256("MN14AU6XWWCgGiRI0mENrK8LrKdi8FhkXB1fhRtoff8=");
        expectedNotificationAttachment.setUrl("https://pn-safestorage-eu-south-1-089813480515.s3.eu-south-1.amazonaws.com" +
                "/PN_NOTIFICATION_ATTACHMENTS-b5eaf1d0b8584ad48fab5a52269f735d.pdf?x-amzn-trace-id=Self%3D1-6537c537-7151c7610d72113c05f72627%3BRoot%3D1-6537c537-" +
                "1f3102c04816313f0c72cefa%3BParent%3D4cdf1151d3da40c1%3BSampled%3D1&X-Amz-Security-" +
                "Token=IQoJb3JpZ2luX2VjEBsaCmV1LXNvdXRoLTEiRzBFAiEAqYfDIukgnbyGoDYIX1h%2B19nRGkAx6HGY%2F7rbBBl3ozUCIGNwjxJmh5tYvL8Nkz9A0tPbaMZRDQ7Vam5QHP75LfvtKo4ECEQQABo" +
                "MMDg5ODEzNDgwNTE1IgwtmV9TjWZ9ZqOSjPMq6wNQCJ15KZf54qce7T%2FKSQZ3e2pRXaTxdlswBqon8Qtp86wzS23RhCRGPAZPO4ksVFK34W7wN9HBgdHKwFmEr4tjVCyagti0NpFZ18hYY6b2wPNDYb%" +
                "2Fzjj%2BCPSWEGzL%2BJU42WUyr%2Fq004LrLDx5CT9HL5p0Do9yglWU5KrymTAEe1Mhr6xAkgHamIDanJEJHkVYQXrRDJ0LQKaWQlwJECjYezLB9MrP5NZz22Peo%2BU8FKNZDbojCj54gRHwYXkzRwv%" +
                "2F0ejH%2F7CTrow4lYmr3uSQboKHhJhY68q62MTZ%2Bq%2F%2BiYcRNevy%2F4xkt45ZE%2BYUIO7dD%2F8SeEPr9DfNCWIA%2FrXhBh%2BZy9UNZeDdwvrkwfvj%2Bq5pSEG5jOn%2FmDN8Ee2DBN9O" +
                "ptycZswrM6xZ8qp3nPNoRK1WSGmDmt9nLJ%2BKYAXyvshYbH5tpI0QsWCRGT8kqNz9PR68FMfLbjfWKnHL1gCFvWQm8uutf%2FV2Z70gmpiY4pAu%2F0ZoxM0LTUWMtg4SGYdtM9XMx0htyM1ZH4XKjZTJc" +
                "TXWwYoFzZkzYON%2BkZqACgLidndvSr3LxsJuznDGbYh36IH8WeoLW%2FTC9xZyF8kxqtfxbMC8vO6BqW7OJNVbl8mtM3P9VehqVIeNrLJ1Mos0bhte6RpBzbwcjFjDjv96pBjqmAbGkC43JdB0ayYOhLo7So" +
                "Fokfw%2FgBl6hr1MWb1gN8JU8NqlFB94tSF9moQWpOQF2KVEOHDsISAlABJBl97LJA%2BqzY0EqBJlwD8eENjqh9KckvxRRQpcX0OZoh5S5KCqoXe3nEvYxeQ4UEhGkSYSLtK%2F%2FQ2vurKjEd1CHnzpRtGV" +
                "xrlqhukQ%2B3qBlp%2BGbuE%2FFBIJhtfuU5nq7uqStPVvjbAq2BHMif80%3D&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20231024T132303Z&X-Amz-SignedHeaders=host&X-Amz-" +
                "Expires=3600&X-Amz-Credential=ASIARJ2KM6RBQBZGT3MO%2F20231024%2Feu-south-1%2Fs3%2Faws4_request&X-Amz-Signature=feef90ebbea0bd69bda117951806342f063c08fe94a98151c" +
                "9383bb3d074ca20");
    }

    @Test
    void getSentNotificationPrivate(){
        SentNotificationV25Dto actual = this.pnDeliveryClient
                .getSentNotificationPrivate("1234").block();

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expectedNotification.getIdempotenceToken(), actual.getIdempotenceToken());
        Assertions.assertEquals(expectedNotification.getPaProtocolNumber(), actual.getPaProtocolNumber());
        Assertions.assertEquals(expectedNotification.getSubject(), actual.getSubject());
        Assertions.assertEquals(expectedNotification.getAbstract(), actual.getAbstract());
        Assertions.assertEquals(expectedNotification.getRecipients(), actual.getRecipients());
        Assertions.assertEquals(expectedNotification.getDocuments(), actual.getDocuments());
        Assertions.assertEquals(expectedNotification.getCancelledIun(), actual.getCancelledIun());
    }

    @Test
    void searchNotificationsPrivate(){
        NotificationSearchResponseDto actual = this.pnDeliveryClient
                .searchNotificationsPrivate(
                        Instant.parse("2023-08-31T15:49:05.630Z"),
                        Instant.parse("2023-10-10T15:49:05.630Z"),
                        "PF-4fc75df3-0913-407e-bdaa-e50329708b7d",
                        null,
                        null,
                        null,
                        1,
                        null
                        ).block();

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expectedNotificationSearchResponse.getResultsPage(), actual.getResultsPage());
        Assertions.assertEquals(expectedNotificationSearchResponse.getNextPagesKey(), actual.getNextPagesKey());
        Assertions.assertEquals(expectedNotificationSearchResponse.getMoreResult(), actual.getMoreResult());
    }

    @Test
    void getReceivedNotificationDocumentPrivate(){
        NotificationAttachmentDownloadMetadataResponseDto actual = this.pnDeliveryClient
                .getReceivedNotificationDocumentPrivate(
                        "PRVZ-NZKM-JEDK-202309-A-1",
                        0,
                        "PF-4fc75df3-0913-407e-bdaa-e50329708b7d",
                        null
                ).block();

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expectedNotificationAttachment.getFilename(), actual.getFilename());
        Assertions.assertEquals(expectedNotificationAttachment.getContentType(), actual.getContentType());
        Assertions.assertEquals(expectedNotificationAttachment.getContentLength(), actual.getContentLength());
        Assertions.assertEquals(expectedNotificationAttachment.getSha256(), actual.getSha256());
        Assertions.assertEquals(expectedNotificationAttachment.getUrl(), actual.getUrl());
    }

    private NotificationDocumentDto getNotificationDocument(String contentType, String refKey, String refVersionToken, String title, String docIdx){
        NotificationDocumentDto document = new NotificationDocumentDto();
        document.setContentType(contentType);
        NotificationAttachmentBodyRefDto notificationAttachmentBodyRefDto1 = new NotificationAttachmentBodyRefDto();
        notificationAttachmentBodyRefDto1.setKey(refKey);
        notificationAttachmentBodyRefDto1.setVersionToken(refVersionToken);
        document.setRef(notificationAttachmentBodyRefDto1);
        document.setTitle(title);
        document.setDocIdx(docIdx);
        return document;
    }

    private NotificationSearchRowDto getNotificationSearchRow(String iun, String paProtocolNumber, String sender, Instant sentAt, String subject, NotificationStatusV26Dto notificationStatus, List<String> recipients, Instant requestAcceptedAt) {
        NotificationSearchRowDto notificationSearchRowDto = new NotificationSearchRowDto();
        notificationSearchRowDto.setIun(iun);
        notificationSearchRowDto.setPaProtocolNumber(paProtocolNumber);
        notificationSearchRowDto.setSender(sender);
        notificationSearchRowDto.setSentAt(sentAt);
        notificationSearchRowDto.setSubject(subject);
        notificationSearchRowDto.setNotificationStatus(notificationStatus);
        notificationSearchRowDto.setRecipients(recipients);
        notificationSearchRowDto.setRequestAcceptedAt(requestAcceptedAt);
        return  notificationSearchRowDto;
    }
    
}
