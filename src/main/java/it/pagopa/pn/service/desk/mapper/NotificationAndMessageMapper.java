package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.*;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.NotificationHistoryResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.TimelineElementCategoryV27Dto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.TimelineElementV27Dto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.*;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.TAX_ID_NOT_FOUND;

public class NotificationAndMessageMapper {

    private NotificationAndMessageMapper(){}

    private static final ModelMapper modelMapper = new ModelMapper();

    public static NotificationResponse getNotification (NotificationSearchRowDto notificationSearchRowDto, List<TimelineElementV27Dto> filteredElements){
        NotificationResponse notification = new NotificationResponse();
        notification.setIun(notificationSearchRowDto.getIun());
        notification.setSender(notificationSearchRowDto.getSender());
        notification.setSentAt(notificationSearchRowDto.getSentAt());
        notification.setSubject(notificationSearchRowDto.getSubject());
        notification.setIunStatus(IunStatus.fromValue(notificationSearchRowDto.getNotificationStatus().getValue()));

            if (!CollectionUtils.isEmpty(filteredElements)){
            List<CourtesyMessage> courtesyMessages = new ArrayList<>();
            filteredElements.forEach(timelineElementDto ->
                    courtesyMessages.add(getCourtesyMessage(timelineElementDto))
            );
            notification.setCourtesyMessages(courtesyMessages);
        }
        return notification;
    }

    public static TimelineResponse getTimeline (NotificationHistoryResponseDto historyResponseDto){
        TimelineResponse response = new TimelineResponse();

        List<TimelineElement> timelineElementList = new ArrayList<>();
        if (historyResponseDto.getTimeline() != null && !historyResponseDto.getTimeline().isEmpty()) {
            filteredElements(historyResponseDto.getTimeline()).forEach(timelineElementDto -> {
                TimelineElement timelineElement = new TimelineElement();
                timelineElement.setCategory(TimelineElementCategory.fromValue(timelineElementDto.getCategory().getValue()));
                timelineElement.setDetail(modelMapper.map(timelineElementDto.getDetails(), TimelineElementDetail.class));
                timelineElement.setTimestamp(timelineElementDto.getEventTimestamp());
                timelineElementList.add(timelineElement);
            });
        }

        response.setTimeline(timelineElementList);
        response.setIunStatus(IunStatus.fromValue(historyResponseDto.getNotificationStatus().getValue()));

        return response;
    }

    private static List<TimelineElementV27Dto> filteredElements(List<TimelineElementV27Dto> timelineElementList){

        return timelineElementList
                .stream()
                .filter(element -> element.getCategory().equals(TimelineElementCategoryV27Dto.REQUEST_ACCEPTED) ||
                        element.getCategory().equals(TimelineElementCategoryV27Dto.SEND_COURTESY_MESSAGE) ||
                        element.getCategory().equals(TimelineElementCategoryV27Dto.SCHEDULE_DIGITAL_WORKFLOW) ||
                        element.getCategory().equals(TimelineElementCategoryV27Dto.SEND_DIGITAL_DOMICILE) ||
                        element.getCategory().equals(TimelineElementCategoryV27Dto.SEND_DIGITAL_PROGRESS) ||
                        element.getCategory().equals(TimelineElementCategoryV27Dto.SEND_DIGITAL_FEEDBACK) ||
                        element.getCategory().equals(TimelineElementCategoryV27Dto.DIGITAL_SUCCESS_WORKFLOW) ||
                        element.getCategory().equals(TimelineElementCategoryV27Dto.DIGITAL_FAILURE_WORKFLOW) ||
                        element.getCategory().equals(TimelineElementCategoryV27Dto.ANALOG_FAILURE_WORKFLOW) ||
                        element.getCategory().equals(TimelineElementCategoryV27Dto.SEND_SIMPLE_REGISTERED_LETTER) ||
                        element.getCategory().equals(TimelineElementCategoryV27Dto.NOTIFICATION_VIEWED) ||
                        element.getCategory().equals(TimelineElementCategoryV27Dto.PREPARE_ANALOG_DOMICILE_FAILURE) ||
                        element.getCategory().equals(TimelineElementCategoryV27Dto.SEND_ANALOG_DOMICILE) ||
                        element.getCategory().equals(TimelineElementCategoryV27Dto.SEND_ANALOG_PROGRESS) ||
                        element.getCategory().equals(TimelineElementCategoryV27Dto.SEND_ANALOG_FEEDBACK) ||
                        element.getCategory().equals(TimelineElementCategoryV27Dto.NOTIFICATION_RADD_RETRIEVED) ||
                        element.getCategory().equals(TimelineElementCategoryV27Dto.COMPLETELY_UNREACHABLE) ||
                        element.getCategory().equals(TimelineElementCategoryV27Dto.AAR_GENERATION) ||
                        element.getCategory().equals(TimelineElementCategoryV27Dto.NOT_HANDLED) ||
                        element.getCategory().equals(TimelineElementCategoryV27Dto.REFINEMENT) ||
                        element.getCategory().equals(TimelineElementCategoryV27Dto.ANALOG_WORKFLOW_RECIPIENT_DECEASED)
                )
                .toList();
    }

    public static Document getDocument(NotificationAttachmentDownloadMetadataResponseDto responseDto){
        Document document = new Document();
        document.setFilename(responseDto.getFilename());
        document.setContentLength(responseDto.getContentLength());
        document.setContentType(responseDto.getContentType());
        return document;
    }

    public static NotificationDetailResponse getNotificationDetail(SentNotificationV25Dto sentNotificationV21Dto) {
        NotificationDetailResponse notificationDetailResponse = new NotificationDetailResponse();
        notificationDetailResponse.setPaProtocolNumber(sentNotificationV21Dto.getPaProtocolNumber());
        notificationDetailResponse.setSubject(sentNotificationV21Dto.getSubject());
        notificationDetailResponse.setAbstract(sentNotificationV21Dto.getAbstract());
        notificationDetailResponse.setIsMultiRecipients(sentNotificationV21Dto.getRecipients().size() > 1);
        if(!sentNotificationV21Dto.getRecipients().isEmpty()){
            notificationDetailResponse.setHasPayments(!sentNotificationV21Dto.getRecipients().get(0).getPayments().isEmpty());
        }
        notificationDetailResponse.setAmount(sentNotificationV21Dto.getAmount());
        notificationDetailResponse.setHasDocuments(!sentNotificationV21Dto.getDocuments().isEmpty());
        notificationDetailResponse.setPhysicalCommunicationType(NotificationDetailResponse.PhysicalCommunicationTypeEnum
                .fromValue(sentNotificationV21Dto.getPhysicalCommunicationType().getValue()));
        notificationDetailResponse.setSenderDenomination(sentNotificationV21Dto.getSenderDenomination());
        notificationDetailResponse.setSenderTaxId(sentNotificationV21Dto.getSenderTaxId());
        notificationDetailResponse.setSentAt(sentNotificationV21Dto.getSentAt());
        notificationDetailResponse.setPaymentExpirationDate(sentNotificationV21Dto.getPaymentExpirationDate());
        return notificationDetailResponse;
    }


    private static CourtesyMessage getCourtesyMessage(TimelineElementV27Dto timelineElementDto) {
        CourtesyMessage courtesyMessage = new CourtesyMessage();
        if (timelineElementDto.getDetails() != null) {
            courtesyMessage.setChannel(CourtesyChannelType.fromValue(timelineElementDto.getDetails().getDigitalAddress().getType()));
            courtesyMessage.setSentTimestamp(timelineElementDto.getDetails().getSendDate());
        }
        return courtesyMessage;
    }

    public static NotificationRecipientDetailResponse getNotificationRecipientDetailResponse(SentNotificationV25Dto sentNotificationV21Dto, String taxId) {
        var response = new NotificationRecipientDetailResponse();
        response.setPaProtocolNumber(sentNotificationV21Dto.getPaProtocolNumber());
        response.setSubject(sentNotificationV21Dto.getSubject());
        response.setAbstract(sentNotificationV21Dto.getAbstract());
        response.setIsMultiRecipients(sentNotificationV21Dto.getRecipients().size() > 1);
        if(!sentNotificationV21Dto.getRecipients().isEmpty()){
            response.setHasPayments(!sentNotificationV21Dto.getRecipients().get(0).getPayments().isEmpty());
        }
        response.setAmount(sentNotificationV21Dto.getAmount());
        response.setHasDocuments(!sentNotificationV21Dto.getDocuments().isEmpty());
        response.setPhysicalCommunicationType(NotificationRecipientDetailResponse.PhysicalCommunicationTypeEnum
                .fromValue(sentNotificationV21Dto.getPhysicalCommunicationType().getValue()));
        response.setSenderDenomination(sentNotificationV21Dto.getSenderDenomination());
        response.setSenderTaxId(sentNotificationV21Dto.getSenderTaxId());
        response.setSentAt(sentNotificationV21Dto.getSentAt());
        response.setPaymentExpirationDate(sentNotificationV21Dto.getPaymentExpirationDate());

        var recipient = sentNotificationV21Dto.getRecipients().stream()
                .filter(recipientV23Dto -> recipientV23Dto.getTaxId().equals(taxId))
                .findFirst()
                .orElseThrow(() -> new PnGenericException(TAX_ID_NOT_FOUND, HttpStatus.BAD_REQUEST));

        response.setRecipient(toNotificationRecipient(recipient));

        return response;
    }

    private static NotificationRecipient toNotificationRecipient(NotificationRecipientV24Dto deliveryRecipient) {
        return new NotificationRecipient()
                .recipientType(NotificationRecipient.RecipientTypeEnum.fromValue(deliveryRecipient.getRecipientType().getValue()))
                .denomination(deliveryRecipient.getDenomination())
                .taxId(deliveryRecipient.getTaxId())
                .payments(toPayments(deliveryRecipient.getPayments()));
    }

    private static List<NotificationPaymentItem> toPayments(List<NotificationPaymentItemDto> deliveryPayments) {
        if(CollectionUtils.isEmpty(deliveryPayments)) {
            return List.of();
        }

        return deliveryPayments.stream()
                .filter(notificationPaymentItemDto -> notificationPaymentItemDto.getPagoPa() != null)
                .map(NotificationAndMessageMapper::toNotificationPaymentItem)
                .toList();
    }

    private static NotificationPaymentItem toNotificationPaymentItem(NotificationPaymentItemDto deliveryPayment) {
        if(deliveryPayment != null) {
            return new NotificationPaymentItem()
                    .pagoPa(toPagoPaPayment(deliveryPayment.getPagoPa()));
        }
        return null;
    }

    private static PagoPaPayment toPagoPaPayment(PagoPaPaymentDto deliveryPagoPaPayment) {
        if(deliveryPagoPaPayment != null) {
            return new PagoPaPayment()
                    .creditorTaxId(deliveryPagoPaPayment.getCreditorTaxId())
                    .noticeCode(deliveryPagoPaPayment.getNoticeCode());
        }
        return null;
    }
}
