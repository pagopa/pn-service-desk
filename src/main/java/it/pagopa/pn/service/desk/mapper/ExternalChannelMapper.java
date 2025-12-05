package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.dto.DigitalCourtesyMailRequestDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pntemplatesengine.v1.dto.LanguageEnumDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pntemplatesengine.v1.dto.NotificationCceForEmailDto;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.templatesengine.PnTemplatesEngineClient;
import lombok.CustomLog;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import it.pagopa.pn.service.desk.utility.Utility;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@CustomLog
public class ExternalChannelMapper {

    private PnTemplatesEngineClient pnTemplatesEngineClient;

    public ExternalChannelMapper(PnTemplatesEngineClient pnTemplatesEngineClient) {
        this.pnTemplatesEngineClient = pnTemplatesEngineClient;
    }

    public  Mono<DigitalCourtesyMailRequestDto> getPrepareCourtesyMail(
            PnServiceDeskOperations operations,
            PnServiceDeskAddress address,
            List<String> attachments,
            String requestId) {

        log.info("Preparing DigitalCourtesyMailRequestDto for requestId: {}", requestId);

        Mono<String> renderedTemplateMono = callNotificationCceForEmail(operations, address, LanguageEnumDto.IT);

        log.info("Received rendered template for requestId: {}", requestId);

        return renderedTemplateMono.map(renderedTemplate -> {
            DigitalCourtesyMailRequestDto mailRequestDto = new DigitalCourtesyMailRequestDto();

            mailRequestDto.setRequestId(requestId);
            mailRequestDto.setCorrelationId(requestId);
            mailRequestDto.setEventType("DEFAULT_EVENT_TYPE");
            mailRequestDto.setQos(DigitalCourtesyMailRequestDto.QosEnum.INTERACTIVE);
            mailRequestDto.setClientRequestTimeStamp(java.time.Instant.now());
            mailRequestDto.setReceiverDigitalAddress(address.getAddress());
            mailRequestDto.setMessageContentType(DigitalCourtesyMailRequestDto.MessageContentTypeEnum.TEXT_HTML);
            mailRequestDto.setChannel(DigitalCourtesyMailRequestDto.ChannelEnum.EMAIL);


            String subject = extractTagContent(renderedTemplate, "title");
            if (subject == null || subject.isEmpty()) {
                subject = "Oggetto della comunicazione";
            }
            mailRequestDto.setSubjectText(subject);
            mailRequestDto.setMessageText(renderedTemplate);


            if (attachments != null && !attachments.isEmpty()) {
                mailRequestDto.setAttachmentUrls(attachments);
            }

            return mailRequestDto;
        });
    }



    private Mono<String> callNotificationCceForEmail(
            PnServiceDeskOperations operations,
            PnServiceDeskAddress address,
            LanguageEnumDto language) {
log.info("Calling PN Templates Engine for notification template for operationId: {}", operations.getOperationId());
        NotificationCceForEmailDto notificationCceForEmailDto = new NotificationCceForEmailDto();
        notificationCceForEmailDto.setDenomination(address.getFullName());
        notificationCceForEmailDto.setIun(operations.getIun());
        notificationCceForEmailDto.setTicketDate(Utility.formatDate(operations.getTicketDate()));
        notificationCceForEmailDto.setVrDate(Utility.formatDate(operations.getVrDate()));

        return this.pnTemplatesEngineClient.notificationCceTemplate(language, notificationCceForEmailDto);
    }

    private String extractTagContent(String html, String tagName) {
        if (html == null || tagName == null) return null;
        Pattern pattern = Pattern.compile("<" + tagName + "[^>]*>(.*?)</" + tagName + ">", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
}
