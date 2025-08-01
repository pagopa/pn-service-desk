package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.dto.DigitalCourtesyMailRequestDto;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import lombok.CustomLog;

import java.util.HashMap;
import java.util.List;

@CustomLog
public class ExternalChannelMapper {

    private ExternalChannelMapper(){};

    public static DigitalCourtesyMailRequestDto getPrepareCourtesyMail(PnServiceDeskOperations operations,
                                                                       PnServiceDeskAddress address,
                                                                       List<String> attachments,
                                                                       String requestId,
                                                                       String fiscalCode,
                                                                       PnServiceDeskConfigs cfn){

        DigitalCourtesyMailRequestDto mailRequestDto = new DigitalCourtesyMailRequestDto();


        mailRequestDto.setRequestId(requestId);
        mailRequestDto.setCorrelationId(requestId);
        mailRequestDto.setEventType("DEFAULT_EVENT_TYPE");
        mailRequestDto.setQos(DigitalCourtesyMailRequestDto.QosEnum.INTERACTIVE);
        mailRequestDto.setClientRequestTimeStamp(java.time.Instant.now());
        mailRequestDto.setReceiverDigitalAddress(address.getAddress());
        mailRequestDto.setMessageContentType(DigitalCourtesyMailRequestDto.MessageContentTypeEnum.PLAIN);
        mailRequestDto.setChannel(DigitalCourtesyMailRequestDto.ChannelEnum.EMAIL);

        //TODO IN ATTESA DI TEMPLATES-ENGINE
        mailRequestDto.setMessageText("Testo Messaggio");
        mailRequestDto.setSubjectText("Oggetto della comunicazione");

        List<String> attachmentUrls = toListStringAttachments(operations);
        if (attachmentUrls != null && !attachmentUrls.isEmpty()) {
            mailRequestDto.setAttachmentUrls(attachmentUrls);
        }

        return mailRequestDto;
    }

    private static List<String> toListStringAttachments(PnServiceDeskOperations operations) {
        if (operations == null || operations.getAttachments() == null) {
            return List.of();
        }
        return operations.getAttachments().stream()
                         .map(fileKey -> "safestorage://" + fileKey)
                         .toList();
    }
}