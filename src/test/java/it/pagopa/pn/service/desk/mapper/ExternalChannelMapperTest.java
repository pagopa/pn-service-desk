package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.dto.DigitalCourtesyMailRequestDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pntemplatesengine.v1.dto.LanguageEnumDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pntemplatesengine.v1.dto.NotificationCceForEmailDto;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.templatesengine.PnTemplatesEngineClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExternalChannelMapperTest {

    private PnTemplatesEngineClient mockTemplatesEngineClient;
    private PnServiceDeskOperations operations;
    private PnServiceDeskAddress address;
    private String requestId;
    private ExternalChannelMapper externalChannelMapper;

    @BeforeEach
    void setup() {
        mockTemplatesEngineClient = Mockito.mock(PnTemplatesEngineClient.class);
        externalChannelMapper = new ExternalChannelMapper(mockTemplatesEngineClient);
        operations = new PnServiceDeskOperations();
        operations.setOperationId("OP123");
        operations.setTicketDate("2025-09-05");
        operations.setVrDate("2025-09-05");

        address = new PnServiceDeskAddress();
        address.setAddress("test@pn.gov.it");
        address.setFullName("Mario Rossi");

        requestId = "REQ-001";
    }



    @Test
    void getPrepareCourtesyMail_shouldReturnValidMailRequest() {
        operations.setOperationId("op123");
        operations.setOperationStartDate(Instant.parse("2023-01-01T10:00:00Z"));
        operations.setOperationLastUpdateDate(Instant.parse("2023-01-02T11:00:00Z"));

        PnServiceDeskAttachments attachment1 = new PnServiceDeskAttachments();
        attachment1.setFilesKey(List.of("safestorage://file1.pdf"));
        PnServiceDeskAttachments attachment2 = new PnServiceDeskAttachments();
        attachment2.setFilesKey(List.of("safestorage://file2.jpg"));
        operations.setAttachments(List.of(attachment1, attachment2));

        String fakeHtmlTemplate = "<title>Test Subject</title><body>Test message</body>";
        Mockito.when(mockTemplatesEngineClient.notificationCceTemplate(Mockito.eq(LanguageEnumDto.IT), Mockito.any(NotificationCceForEmailDto.class)))
               .thenReturn(Mono.just(fakeHtmlTemplate));

        Mono<DigitalCourtesyMailRequestDto> resultMono = externalChannelMapper.getPrepareCourtesyMail(
                operations,
                address,
                List.of("safestorage://file1.pdf", "safestorage://file2.jpg"),
                requestId);

        StepVerifier.create(resultMono)
                    .assertNext(mailRequestDto -> {
                        assertEquals(requestId, mailRequestDto.getRequestId());
                        assertEquals(requestId, mailRequestDto.getCorrelationId());
                        assertEquals("DEFAULT_EVENT_TYPE", mailRequestDto.getEventType());
                        assertEquals(DigitalCourtesyMailRequestDto.QosEnum.INTERACTIVE, mailRequestDto.getQos());
                        assertEquals(address.getAddress(), mailRequestDto.getReceiverDigitalAddress());
                        assertEquals(DigitalCourtesyMailRequestDto.MessageContentTypeEnum.TEXT_HTML, mailRequestDto.getMessageContentType());
                        assertEquals(DigitalCourtesyMailRequestDto.ChannelEnum.EMAIL, mailRequestDto.getChannel());
                        assertEquals("Test Subject", mailRequestDto.getSubjectText());
                        assertEquals(fakeHtmlTemplate, mailRequestDto.getMessageText());

                        List<String> expectedAttachments = List.of("safestorage://file1.pdf", "safestorage://file2.jpg");
                        assertEquals(expectedAttachments, mailRequestDto.getAttachmentUrls());
                    })
                    .verifyComplete();

        Mockito.verify(mockTemplatesEngineClient).notificationCceTemplate(Mockito.eq(LanguageEnumDto.IT), Mockito.any(NotificationCceForEmailDto.class));
    }

    @Test
    void extractTagContent_shouldReturnCorrectContent() throws Exception {
        String html = "<title>Subject here</title><p>Test</p>";
        String tagName = "title";

        ExternalChannelMapper mapper = new ExternalChannelMapper(mockTemplatesEngineClient);

        var method = ExternalChannelMapper.class.getDeclaredMethod("extractTagContent", String.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(mapper, html, tagName);

        assertNotNull(result, "Il contenuto non dovrebbe essere null");
        assertEquals("Subject here", result.trim(), "Il contenuto estratto dal tag non è corretto");
    }


    @Test
    void testGenerateMailWithoutAttachments() {
        Mockito.when(mockTemplatesEngineClient.notificationCceTemplate(Mockito.any(), Mockito.any()))
               .thenReturn(Mono.just("<title>Email di Test</title>Contenuto email"));

        DigitalCourtesyMailRequestDto result = externalChannelMapper
                .getPrepareCourtesyMail(operations, address, null, requestId)
                .block();

        assertNotNull(result);
        assertEquals("Email di Test", result.getSubjectText());
        assertEquals("test@pn.gov.it", result.getReceiverDigitalAddress());
        assertTrue(result.getAttachmentUrls() == null || result.getAttachmentUrls().isEmpty());
    }

    @Test
    void testGenerateMailWithEmptyAttachments() {
        Mockito.when(mockTemplatesEngineClient.notificationCceTemplate(Mockito.any(), Mockito.any()))
               .thenReturn(Mono.just("<title>Email di Test</title>Contenuto email"));

        DigitalCourtesyMailRequestDto result = externalChannelMapper
                .getPrepareCourtesyMail(operations, address, List.of(), requestId)
                .block();

        assertNotNull(result);
        assertTrue(result.getAttachmentUrls() == null || result.getAttachmentUrls().isEmpty());
    }

    @Test
    void testGenerateMailWithAttachments() {
        List<String> attachments = List.of("safestorage://file1.pdf", "safestorage://file2.jpg");

        Mockito.when(mockTemplatesEngineClient.notificationCceTemplate(Mockito.any(), Mockito.any()))
               .thenReturn(Mono.just("<title>Email di Test</title>Contenuto email"));

        DigitalCourtesyMailRequestDto result = externalChannelMapper
                .getPrepareCourtesyMail(operations, address, attachments, requestId)
                .block();

        assertNotNull(result);
        assertEquals(List.of("safestorage://file1.pdf", "safestorage://file2.jpg"), result.getAttachmentUrls());
    }

    @Test
    void testFallbackSubjectWhenMissing() {
        String htmlWithoutSubject = "<html><body>No title here</body></html>";

        Mockito.when(mockTemplatesEngineClient.notificationCceTemplate(Mockito.any(), Mockito.any()))
               .thenReturn(Mono.just(htmlWithoutSubject));

        DigitalCourtesyMailRequestDto result = externalChannelMapper
                .getPrepareCourtesyMail(operations, address, List.of("file1.txt"), requestId)
                .block();

        assertNotNull(result);
        assertEquals("Oggetto della comunicazione", result.getSubjectText());
    }
}
