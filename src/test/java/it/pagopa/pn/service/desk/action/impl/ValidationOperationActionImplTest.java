package it.pagopa.pn.service.desk.action.impl;


import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnaddressmanager.v1.dto.DeduplicatesResponseDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationAttachmentBodyRefDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.NotificationDocumentDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndelivery.v1.dto.SentNotificationDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.LegalFactCategoryDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.LegalFactListElementDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.LegalFactsIdDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.ResponsePaperNotificationFailedDtoDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.PaperChannelUpdateDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.PrepareEventDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.PrepareRequestDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.ProposalTypeEnumDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.service.desk.middleware.db.dao.AddressDAO;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.addressmanager.PnAddressManagerClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.paperchannel.PnPaperChannelClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.safestorage.PnSafeStorageClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ValidationOperationActionImplTest {

    @InjectMocks
    private ValidationOperationActionImpl validationOperationAction;

    @Mock
    private OperationDAO operationDAO;

    @Mock
    private AddressDAO addressDAO;

    @Mock
    private PnAddressManagerClient addressManagerClient;

    @Mock
    private PnDeliveryPushClient pnDeliveryPushClient;

    @Mock
    private PnDeliveryClient pnDeliveryClient;

    @Mock
    private PnPaperChannelClient paperChannelClient;

    @Mock
    private PnSafeStorageClient safeStorageClient;

    @Mock
    private PnServiceDeskConfigs cfn;

    private final PnServiceDeskOperations pnServiceDeskOperations = new PnServiceDeskOperations();
    private final ResponsePaperNotificationFailedDtoDto responsePaperNotificationFailedDtoDto = new ResponsePaperNotificationFailedDtoDto();
    private final SentNotificationDto sentNotificationDto = new SentNotificationDto();
    private final NotificationDocumentDto notificationDocumentDto = new NotificationDocumentDto();
    private final List<NotificationDocumentDto> notifications = new ArrayList<>();
    private final NotificationAttachmentBodyRefDto notificationAttachmentBodyRefDto = new NotificationAttachmentBodyRefDto();
    private final LegalFactListElementDto legalFactListElementDto = new LegalFactListElementDto();
    private final LegalFactsIdDto legalFactsIdDto = new LegalFactsIdDto();
    private final PnServiceDeskAttachments pnServiceDeskAttachments = new PnServiceDeskAttachments();
    private final List<PnServiceDeskAttachments> pnServiceDeskAttachmentsList  = new ArrayList<>();
    private final List<String> fileKeys = new ArrayList<>();
    private final PaperChannelUpdateDto paperChannelUpdateDto = new PaperChannelUpdateDto();
    private final PrepareEventDto prepareEventDto = new PrepareEventDto();
    private final DeduplicatesResponseDto deduplicatesResponseDto = new DeduplicatesResponseDto();
    private final FileDownloadResponse fileDownloadResponse = new FileDownloadResponse();


    @BeforeEach
    public void init(){
        fileDownloadResponse.setKey("key1234");
        fileDownloadResponse.setChecksum("checkSum");
        fileDownloadResponse.setContentType("contentType");
        fileDownloadResponse.setRetentionUntil(null);
        fileDownloadResponse.setDocumentType("documentType");
        deduplicatesResponseDto.setEqualityResult(Boolean.FALSE);
        paperChannelUpdateDto.setPrepareEvent(prepareEventDto);
        pnServiceDeskAttachments.setIsAvailable(Boolean.TRUE);
        fileKeys.add("fileKeyAttachment");
        pnServiceDeskAttachments.setFilesKey(fileKeys);
        pnServiceDeskAttachments.setIun("iunAttachment");
        pnServiceDeskAttachmentsList.add(pnServiceDeskAttachments);
        pnServiceDeskOperations.setOperationId("opId1234");
        pnServiceDeskOperations.setStatus("status");
        pnServiceDeskOperations.setTicketId("ticketId");
        pnServiceDeskOperations.setOperationStartDate(Instant.now());
        pnServiceDeskOperations.setEvents(null);
        pnServiceDeskOperations.setAttachments(pnServiceDeskAttachmentsList );
        pnServiceDeskOperations.setOperationLastUpdateDate(Instant.now());
        pnServiceDeskOperations.setErrorReason("errorReason");
        pnServiceDeskOperations.setRecipientInternalId("recipientInternalId");

        responsePaperNotificationFailedDtoDto.setIun("iunResponse");
        legalFactsIdDto.setKey("keyLegalFact");
        legalFactsIdDto.setCategory(LegalFactCategoryDto.ANALOG_DELIVERY);
        legalFactListElementDto.setLegalFactsId(legalFactsIdDto);
        notificationAttachmentBodyRefDto.setKey("KeyRef");
        notificationAttachmentBodyRefDto.setVersionToken("versionTokenRef");
        notificationDocumentDto.setRef(notificationAttachmentBodyRefDto);
        notifications.add(notificationDocumentDto);
        sentNotificationDto.setDocuments(notifications);
    }

    @Test
    void executeWithoutOperationId(){
        Mockito.when(this.operationDAO.getByOperationId("opId1234")).thenReturn(Mono.empty());
        Assertions.assertThrows(PnGenericException.class, () -> {
            this.validationOperationAction.execute("opId1234");
        });
    }

    @Test
    void executeGetAddressNotFound(){
        Mockito.when(this.operationDAO.getByOperationId("opId1234")).thenReturn(Mono.just(pnServiceDeskOperations));
        Mockito.when(this.addressDAO.getAddress("opId1234")).thenReturn(Mono.empty());
        Assertions.assertThrows(PnGenericException.class, () -> {
            this.validationOperationAction.execute("opId1234");
        });
    }

    @Test
    void executePnDeliveryPushClient(){
        String errorMessage = "Error message";
        PnGenericException exception = new PnGenericException(null,null);
        Mockito.when(this.operationDAO.getByOperationId("opId1234")).thenReturn(Mono.just(pnServiceDeskOperations));
        Mockito.when(this.addressDAO.getAddress("opId1234")).thenReturn(Mono.just(new PnServiceDeskAddress()));
        Mockito.when(addressManagerClient.deduplicates(Mockito.any())).thenReturn(Mono.just(new DeduplicatesResponseDto()));
        Mockito.when(this.pnDeliveryPushClient.paperNotificationFailed(Mockito.any())).thenThrow(exception);
        Assertions.assertThrows(PnGenericException.class, () -> {
            this.validationOperationAction.execute("opId1234");
        });
    }

    @Test
    void executeAddressManagerClientEqualityResultFalse(){
        Mockito.when(this.operationDAO.getByOperationId("opId1234")).thenReturn(Mono.just(pnServiceDeskOperations));
        Mockito.when(this.addressDAO.getAddress("opId1234")).thenReturn(Mono.just(new PnServiceDeskAddress()));
        Mockito.when(this.addressManagerClient.deduplicates(Mockito.any())).thenReturn(Mono.just(deduplicatesResponseDto));
        Assertions.assertThrows(PnGenericException.class, () -> {
            this.validationOperationAction.execute("opId1234");
        });
    }

    @Test
    void executeAddressManagerClientErrorNotBlank(){
        deduplicatesResponseDto.setError("error message");
        deduplicatesResponseDto.setEqualityResult(Boolean.TRUE);
        Mockito.when(this.operationDAO.getByOperationId("opId1234")).thenReturn(Mono.just(pnServiceDeskOperations));
        Mockito.when(this.addressDAO.getAddress("opId1234")).thenReturn(Mono.just(new PnServiceDeskAddress()));
        Mockito.when(this.addressManagerClient.deduplicates(Mockito.any())).thenReturn(Mono.just(deduplicatesResponseDto));
        Assertions.assertThrows(PnGenericException.class, () -> {
            this.validationOperationAction.execute("opId1234");
        });
    }


    @Test
    void executeUpdateOperationStatusEmpty(){
        Mockito.when(this.operationDAO.getByOperationId("opId1234")).thenReturn(Mono.just(pnServiceDeskOperations));
        Mockito.when(this.addressDAO.getAddress("opId1234")).thenReturn(Mono.just(new PnServiceDeskAddress()));
        Mockito.when(this.addressManagerClient.deduplicates(Mockito.any())).thenReturn(Mono.just(new DeduplicatesResponseDto()));
        Mockito.when(this.pnDeliveryPushClient.paperNotificationFailed(Mockito.any())).thenReturn(Flux.just(responsePaperNotificationFailedDtoDto));
        Mockito.when(this.pnDeliveryClient.getSentNotificationPrivate(Mockito.any())).thenReturn(Mono.just(sentNotificationDto));
        Mockito.when(this.operationDAO.updateEntity(Mockito.any())).thenReturn(Mono.empty());
        Mockito.when(this.pnDeliveryPushClient.getNotificationLegalFactsPrivate(Mockito.any(),Mockito.any())).thenReturn(Flux.just(legalFactListElementDto));
        Assertions.assertThrows(PnGenericException.class, () -> {
            this.validationOperationAction.execute("opId1234");
        });
    }

    @Test
    void executeGetNotificationLegalFactsPrivateEmpty(){
        PnGenericException exception = new PnGenericException(null,null);
        Mockito.when(this.operationDAO.getByOperationId("opId1234")).thenReturn(Mono.just(pnServiceDeskOperations));
        Mockito.when(this.addressDAO.getAddress("opId1234")).thenReturn(Mono.just(new PnServiceDeskAddress()));
        Mockito.when(this.addressManagerClient.deduplicates(Mockito.any())).thenReturn(Mono.just(new DeduplicatesResponseDto()));
        Mockito.when(this.pnDeliveryPushClient.paperNotificationFailed(Mockito.any())).thenReturn(Flux.just(responsePaperNotificationFailedDtoDto));
        Mockito.when(this.pnDeliveryClient.getSentNotificationPrivate(Mockito.any())).thenReturn(Mono.just(sentNotificationDto));
        Mockito.when(this.operationDAO.updateEntity(Mockito.any())).thenReturn(Mono.just(pnServiceDeskOperations));
        Mockito.when(this.pnDeliveryPushClient.getNotificationLegalFactsPrivate(Mockito.any(),Mockito.any())).thenThrow(exception);
        Assertions.assertThrows(PnGenericException.class, () -> {
            this.validationOperationAction.execute("opId1234");
        });
    }

    @Test
    void executeGetNotificationLegalFactsPrivateEmptyAndIsAvailableFalse(){
        pnServiceDeskOperations.getAttachments().get(0).setIsAvailable(Boolean.FALSE);
        PnGenericException exception = new PnGenericException(null,null);
        Mockito.when(this.operationDAO.getByOperationId("opId1234")).thenReturn(Mono.just(pnServiceDeskOperations));
        Mockito.when(this.addressDAO.getAddress("opId1234")).thenReturn(Mono.just(new PnServiceDeskAddress()));
        Mockito.when(this.addressManagerClient.deduplicates(Mockito.any())).thenReturn(Mono.just(new DeduplicatesResponseDto()));
        Mockito.when(this.pnDeliveryPushClient.paperNotificationFailed(Mockito.any())).thenReturn(Flux.just(responsePaperNotificationFailedDtoDto));
        Mockito.when(this.pnDeliveryClient.getSentNotificationPrivate(Mockito.any())).thenReturn(Mono.just(sentNotificationDto));
        Mockito.when(this.operationDAO.updateEntity(Mockito.any())).thenReturn(Mono.just(pnServiceDeskOperations));
        Mockito.when(this.pnDeliveryPushClient.getNotificationLegalFactsPrivate(Mockito.any(),Mockito.any())).thenThrow(exception);
        Assertions.assertThrows(PnGenericException.class, () -> {
            this.validationOperationAction.execute("opId1234");
        });
    }

    @Test
    void executeGetNotificationLegalFactsPrivate(){
        Mockito.when(this.operationDAO.getByOperationId("opId1234")).thenReturn(Mono.just(pnServiceDeskOperations));
        Mockito.when(this.addressDAO.getAddress("opId1234")).thenReturn(Mono.just(new PnServiceDeskAddress()));
        Mockito.when(this.addressManagerClient.deduplicates(Mockito.any())).thenReturn(Mono.just(new DeduplicatesResponseDto()));
        Mockito.when(this.pnDeliveryPushClient.paperNotificationFailed(Mockito.any())).thenReturn(Flux.just(responsePaperNotificationFailedDtoDto));
        Mockito.when(this.pnDeliveryClient.getSentNotificationPrivate(Mockito.any())).thenReturn(Mono.just(sentNotificationDto));
        Mockito.when(this.operationDAO.updateEntity(Mockito.any())).thenReturn(Mono.just(pnServiceDeskOperations));
        Mockito.when(this.pnDeliveryPushClient.getNotificationLegalFactsPrivate(Mockito.any(),Mockito.any())).thenReturn(Flux.just(legalFactListElementDto));
        Mockito.when(this.paperChannelClient.sendPaperPrepareRequest(Mockito.any(),Mockito.any())).thenReturn(Mono.just(paperChannelUpdateDto));

        this.validationOperationAction.execute("opId1234");

        ArgumentCaptor<PrepareRequestDto> capturePrepareRequest = ArgumentCaptor.forClass(PrepareRequestDto.class);
        Mockito.verify(paperChannelClient).sendPaperPrepareRequest(Mockito.any(),capturePrepareRequest.capture());

        Assertions.assertNotNull(capturePrepareRequest.getValue().getRequestId());
        Assertions.assertNotNull(capturePrepareRequest.getValue().getIun());
        Assertions.assertEquals("opId1234", capturePrepareRequest.getValue().getIun());
        Assertions.assertEquals("PF", capturePrepareRequest.getValue().getReceiverType());
        Assertions.assertEquals("SERVICE_DESK_OPID-opId1234", capturePrepareRequest.getValue().getRequestId());
        Assertions.assertEquals(ProposalTypeEnumDto.RS, capturePrepareRequest.getValue().getProposalProductType());

    }


}