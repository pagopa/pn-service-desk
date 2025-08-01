package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.dto.DigitalCourtesyMailRequestDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pntemplatesengine.v1.dto.LanguageEnumDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pntemplatesengine.v1.dto.NotificationCceForEmailDto;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.templatesengine.PnTemplatesEngineClient;
import lombok.CustomLog;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CustomLog
public class ExternalChannelMapper {

    private ExternalChannelMapper() {}

    private static PnTemplatesEngineClient pnTemplatesEngineClient;

    public static void setPnTemplatesEngineClient(PnTemplatesEngineClient client) {
        pnTemplatesEngineClient = client;
    }

    public static Mono<DigitalCourtesyMailRequestDto> getPrepareCourtesyMail(
            PnServiceDeskOperations operations,
            PnServiceDeskAddress address,
            List<String> attachments,
            String requestId,
            String fiscalCode,
            PnServiceDeskConfigs cfn) {

        Mono<String> renderedTemplateMono = callNotificationCceForEmail(operations, address, LanguageEnumDto.IT);

        return renderedTemplateMono.map(renderedTemplate -> {
            DigitalCourtesyMailRequestDto mailRequestDto = new DigitalCourtesyMailRequestDto();

            mailRequestDto.setRequestId(requestId);
            mailRequestDto.setCorrelationId(requestId);
            mailRequestDto.setEventType("DEFAULT_EVENT_TYPE");
            mailRequestDto.setQos(DigitalCourtesyMailRequestDto.QosEnum.INTERACTIVE);
            mailRequestDto.setClientRequestTimeStamp(java.time.Instant.now());
            mailRequestDto.setReceiverDigitalAddress(address.getAddress());
            mailRequestDto.setMessageContentType(DigitalCourtesyMailRequestDto.MessageContentTypeEnum.PLAIN);
            mailRequestDto.setChannel(DigitalCourtesyMailRequestDto.ChannelEnum.EMAIL);

            // Estraggo il subject dal tag <mj-title>
            String subject = extractTagContent(renderedTemplate, "mj-title");
            if (subject == null || subject.isEmpty()) {
                subject = "Oggetto della comunicazione"; // fallback
            }
            mailRequestDto.setSubjectText(subject);

            // Per messageText puoi decidere se mettere tutto l’HTML o solo una parte, qui metto tutto
            mailRequestDto.setMessageText(renderedTemplate);

            List<String> attachmentUrls = toListStringAttachments(operations);
            if (attachmentUrls != null && !attachmentUrls.isEmpty()) {
                mailRequestDto.setAttachmentUrls(attachmentUrls);
            }

            return mailRequestDto;
        });
    }

    private static List<String> toListStringAttachments(PnServiceDeskOperations operations) {
        if (operations == null || operations.getAttachments() == null) {
            return List.of();
        }
        return operations.getAttachments().stream()
                         .filter(attachment -> attachment.getFilesKey() != null)
                         .flatMap(attachment -> attachment.getFilesKey().stream())
                         .map(fileKey -> "safestorage://" + fileKey)
                         .toList();
    }


    private static Mono<String> callNotificationCceForEmail(
            PnServiceDeskOperations operations,
            PnServiceDeskAddress address,
            LanguageEnumDto language) {

        NotificationCceForEmailDto notificationCceForEmailDto = new NotificationCceForEmailDto();
        notificationCceForEmailDto.setDenomination(address.getFullName());
        notificationCceForEmailDto.setIun(operations.getOperationId());
        notificationCceForEmailDto.setTicketDate(String.valueOf(operations.getOperationStartDate()));
        notificationCceForEmailDto.setVrDate(String.valueOf(operations.getOperationLastUpdateDate()));

        return pnTemplatesEngineClient.notificationCceTemplate(language, notificationCceForEmailDto);
    }

    private static String extractTagContent(String html, String tagName) {
        if (html == null || tagName == null) return null;
        Pattern pattern = Pattern.compile("<" + tagName + "[^>]*>(.*?)</" + tagName + ">", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
}
