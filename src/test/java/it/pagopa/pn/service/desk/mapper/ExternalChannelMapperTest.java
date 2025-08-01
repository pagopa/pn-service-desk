package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.dto.DigitalCourtesyMailRequestDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pntemplatesengine.v1.dto.LanguageEnumDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pntemplatesengine.v1.dto.NotificationCceForEmailDto;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.templatesengine.PnTemplatesEngineClient;
import org.junit.jupiter.api.AfterEach;
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

    @BeforeEach
    void setup() {
        mockTemplatesEngineClient = Mockito.mock(PnTemplatesEngineClient.class);
        ExternalChannelMapper.setPnTemplatesEngineClient(mockTemplatesEngineClient);
    }

    @AfterEach
    void tearDown() {
        // Reset static field to avoid side effects on other tests
        ExternalChannelMapper.setPnTemplatesEngineClient(null);
    }

    @Test
    void getPrepareCourtesyMail_shouldReturnValidMailRequest() {
        // Prepara dati
        PnServiceDeskOperations operations = new PnServiceDeskOperations();
        operations.setOperationId("op123");
        operations.setOperationStartDate(Instant.parse("2023-01-01T10:00:00Z"));
        operations.setOperationLastUpdateDate(Instant.parse("2023-01-02T11:00:00Z"));
        PnServiceDeskAttachments attachment1 = new PnServiceDeskAttachments();
        attachment1.setFilesKey(List.of("file1.pdf"));

        PnServiceDeskAttachments attachment2 = new PnServiceDeskAttachments();
        attachment2.setFilesKey(List.of("file2.jpg"));

        operations.setAttachments(List.of(attachment1, attachment2));

        PnServiceDeskAddress address = new PnServiceDeskAddress();
        address.setAddress("test@example.com");
        address.setFullName("Mario Rossi");

        String requestId = "req123";
        String fiscalCode = "RSSMRA80A01H501U";
        PnServiceDeskConfigs configs = Mockito.mock(PnServiceDeskConfigs.class);

        // Mock template engine client
        String fakeHtmlTemplate = "<mj-title>Test Subject</mj-title><body>Test message</body>";
        Mockito.when(mockTemplatesEngineClient.notificationCceTemplate(Mockito.eq(LanguageEnumDto.IT), Mockito.any(NotificationCceForEmailDto.class)))
               .thenReturn(Mono.just(fakeHtmlTemplate));

        // Esegui metodo
        Mono<DigitalCourtesyMailRequestDto> resultMono = ExternalChannelMapper.getPrepareCourtesyMail(
                operations,
                address,
                List.of("file1.pdf", "file2.jpg"),
                requestId,
                fiscalCode,
                configs
                                                                                                     );

        // Verifica risultato con StepVerifier
        StepVerifier.create(resultMono)
                    .assertNext(mailRequestDto -> {
                        assertEquals(requestId, mailRequestDto.getRequestId());
                        assertEquals(requestId, mailRequestDto.getCorrelationId());
                        assertEquals("DEFAULT_EVENT_TYPE", mailRequestDto.getEventType());
                        assertEquals(DigitalCourtesyMailRequestDto.QosEnum.INTERACTIVE, mailRequestDto.getQos());
                        assertEquals(address.getAddress(), mailRequestDto.getReceiverDigitalAddress());
                        assertEquals(DigitalCourtesyMailRequestDto.MessageContentTypeEnum.PLAIN, mailRequestDto.getMessageContentType());
                        assertEquals(DigitalCourtesyMailRequestDto.ChannelEnum.EMAIL, mailRequestDto.getChannel());
                        assertEquals("Test Subject", mailRequestDto.getSubjectText());
                        assertEquals(fakeHtmlTemplate, mailRequestDto.getMessageText());

                        List<String> expectedAttachments = List.of("safestorage://file1.pdf", "safestorage://file2.jpg");
                        assertEquals(expectedAttachments, mailRequestDto.getAttachmentUrls());
                    })
                    .verifyComplete();

        // Verifica chiamata al client
        Mockito.verify(mockTemplatesEngineClient).notificationCceTemplate(Mockito.eq(LanguageEnumDto.IT), Mockito.any(NotificationCceForEmailDto.class));
    }

    @Test
    void extractTagContent_shouldReturnCorrectContent() throws Exception {
        // Metodo privato, testiamo indirettamente tramite riflessione

        String html = "<mj-title>Subject here</mj-title><p>Test</p>";
        String tagName = "mj-title";

        // Uso reflection perché metodo è privato statico
        var method = ExternalChannelMapper.class.getDeclaredMethod("extractTagContent", String.class, String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(null, html, tagName);

        assertEquals("Subject here", result);
    }

    @Test
    void toListStringAttachments_shouldReturnEmptyListIfNull() throws Exception {
        // Metodo privato statico test tramite reflection

        var method = ExternalChannelMapper.class.getDeclaredMethod("toListStringAttachments", PnServiceDeskOperations.class);
        method.setAccessible(true);

        PnServiceDeskOperations nullAttachmentsOps = new PnServiceDeskOperations();
        nullAttachmentsOps.setAttachments(null);

        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) method.invoke(null, nullAttachmentsOps);

        assertTrue(result.isEmpty());
    }

}
