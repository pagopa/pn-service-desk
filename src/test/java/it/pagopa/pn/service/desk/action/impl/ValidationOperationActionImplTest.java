package it.pagopa.pn.service.desk.action.impl;


import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.exception.ExceptionTypeEnum;
import it.pagopa.pn.service.desk.exception.PnEntityNotFoundException;
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
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileDownloadInfo;
import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileDownloadResponse;
import it.pagopa.pn.service.desk.middleware.db.dao.AddressDAO;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.addressmanager.PnAddressManagerClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.datavault.PnDataVaultClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.delivery.PnDeliveryClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.deliverypush.PnDeliveryPushClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.paperchannel.PnPaperChannelClient;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.safestorage.PnSafeStorageClient;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
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
    private PnDataVaultClient pnDataVaultClient;

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
    private final List<PnServiceDeskAttachments> pnServiceDeskAttachmentsList = new ArrayList<>();
    private final List<String> fileKeys = new ArrayList<>();
    private final PaperChannelUpdateDto paperChannelUpdateDto = new PaperChannelUpdateDto();
    private final PrepareEventDto prepareEventDto = new PrepareEventDto();
    private final DeduplicatesResponseDto deduplicatesResponseDto = new DeduplicatesResponseDto();
    private final FileDownloadResponse fileDownloadResponse = new FileDownloadResponse();


    @BeforeEach
    public void init() {
        fileDownloadResponse.setKey("key1234");
        fileDownloadResponse.setChecksum("checkSum");
        fileDownloadResponse.setContentType("contentType");
        fileDownloadResponse.setRetentionUntil(null);
        fileDownloadResponse.setDocumentType("documentType");
        deduplicatesResponseDto.setEqualityResult(Boolean.FALSE);
        paperChannelUpdateDto.setPrepareEvent(prepareEventDto);
        fileKeys.add("fileKeyAttachment");

        pnServiceDeskAttachments.setIsAvailable(Boolean.TRUE);
        pnServiceDeskAttachments.setFilesKey(fileKeys);
        pnServiceDeskAttachments.setIun("iunAttachment");
        pnServiceDeskAttachmentsList.add(pnServiceDeskAttachments);
        pnServiceDeskOperations.setOperationId("opId1234");
        pnServiceDeskOperations.setStatus("status");
        pnServiceDeskOperations.setTicketId("ticketId");
        pnServiceDeskOperations.setOperationStartDate(Instant.now());
        pnServiceDeskOperations.setEvents(null);
        pnServiceDeskOperations.setAttachments(pnServiceDeskAttachmentsList);
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
    void executeWithoutOperationId() {
        Mockito.when(this.operationDAO.getByOperationId("opId1234")).thenReturn(Mono.empty());
        Assertions.assertThrows(PnEntityNotFoundException.class, () -> {
            this.validationOperationAction.execute("opId1234");
        });
    }

    @Test
    void executeGetAddressNotFound() {
        Mockito.when(this.operationDAO.getByOperationId("opId1234")).thenReturn(Mono.just(pnServiceDeskOperations));
        Mockito.when(this.addressDAO.getAddress("opId1234")).thenReturn(Mono.empty());
        Mockito.when(this.operationDAO.updateEntity(Mockito.any())).thenReturn(Mono.just(pnServiceDeskOperations));

        Assertions.assertDoesNotThrow(() -> {
            this.validationOperationAction.execute("opId1234");
        });

        ArgumentCaptor<PnServiceDeskOperations> captureOperations = ArgumentCaptor.forClass(PnServiceDeskOperations.class);
        Mockito.verify(operationDAO).updateEntity(captureOperations.capture());

        Assertions.assertNotNull(captureOperations.getValue());
        Assertions.assertEquals(OperationStatusEnum.KO.toString(), captureOperations.getValue().getStatus());
        Assertions.assertNotNull(captureOperations.getValue().getErrorReason());
    }

    @Test
    void executeAddressNotValidThrowGenericException() {
        Mockito.when(this.operationDAO.getByOperationId("opId1234"))
                .thenReturn(Mono.just(pnServiceDeskOperations));

        Mockito.when(this.addressDAO.getAddress("opId1234"))
                .thenReturn(Mono.just(new PnServiceDeskAddress()));

        Mockito.when(this.addressManagerClient.deduplicates(Mockito.any()))
                .thenReturn(Mono.just(getDeduplicatesResponseWithError()));

        Mockito.when(this.operationDAO.updateEntity(Mockito.any()))
                .thenReturn(Mono.just(pnServiceDeskOperations));

        Assertions.assertDoesNotThrow(() -> this.validationOperationAction.execute("opId1234"));

        ArgumentCaptor<PnServiceDeskOperations> captureOperations = ArgumentCaptor.forClass(PnServiceDeskOperations.class);
        Mockito.verify(operationDAO).updateEntity(captureOperations.capture());

        Assertions.assertNotNull(captureOperations.getValue());
        Assertions.assertEquals(OperationStatusEnum.KO.toString(), captureOperations.getValue().getStatus());
        Assertions.assertEquals(ExceptionTypeEnum.ADDRESS_IS_NOT_VALID.getMessage(), captureOperations.getValue().getErrorReason());
    }

    @Test
    void executeAddressNotEqualsThrowGenericException() {
        Mockito.when(this.operationDAO.getByOperationId("opId1234"))
                .thenReturn(Mono.just(pnServiceDeskOperations));

        Mockito.when(this.addressDAO.getAddress("opId1234"))
                .thenReturn(Mono.just(new PnServiceDeskAddress()));

        Mockito.when(this.addressManagerClient.deduplicates(Mockito.any()))
                .thenReturn(Mono.just(getDeduplicatesResponse(false)));

        Mockito.when(this.operationDAO.updateEntity(Mockito.any()))
                .thenReturn(Mono.just(pnServiceDeskOperations));

        Assertions.assertDoesNotThrow(() -> this.validationOperationAction.execute("opId1234"));

        ArgumentCaptor<PnServiceDeskOperations> captureOperations = ArgumentCaptor.forClass(PnServiceDeskOperations.class);
        Mockito.verify(operationDAO).updateEntity(captureOperations.capture());

        Assertions.assertNotNull(captureOperations.getValue());
        Assertions.assertEquals(OperationStatusEnum.KO.toString(), captureOperations.getValue().getStatus());
        Assertions.assertEquals(ExceptionTypeEnum.ADDRESS_IS_NOT_VALID.getMessage(), captureOperations.getValue().getErrorReason());
    }

    @Test
    void executePnDeliveryPushClient() {
        PnGenericException exception = new PnGenericException(ExceptionTypeEnum.BAD_REQUEST, ExceptionTypeEnum.BAD_REQUEST.getMessage());
        Mockito.when(this.operationDAO.getByOperationId("opId1234")).thenReturn(Mono.just(pnServiceDeskOperations));
        Mockito.when(this.addressDAO.getAddress("opId1234")).thenReturn(Mono.just(new PnServiceDeskAddress()));
        Mockito.when(addressManagerClient.deduplicates(Mockito.any())).thenReturn(Mono.just(new DeduplicatesResponseDto()));
        Mockito.when(this.pnDeliveryPushClient.paperNotificationFailed(Mockito.any())).thenThrow(exception);
        Mockito.when(this.operationDAO.updateEntity(Mockito.any()))
                .thenReturn(Mono.just(pnServiceDeskOperations));
        Assertions.assertDoesNotThrow(() ->
                this.validationOperationAction.execute("opId1234")
        );
        ArgumentCaptor<PnServiceDeskOperations> captureOperations = ArgumentCaptor.forClass(PnServiceDeskOperations.class);
        Mockito.verify(operationDAO).updateEntity(captureOperations.capture());

        Assertions.assertNotNull(captureOperations.getValue());
        Assertions.assertEquals(OperationStatusEnum.KO.toString(), captureOperations.getValue().getStatus());
        Assertions.assertEquals(ExceptionTypeEnum.BAD_REQUEST.getMessage(), captureOperations.getValue().getErrorReason());
    }

    @Test
    void executeUpdateOperationStatusEmpty() {
        Mockito.when(this.operationDAO.getByOperationId("opId1234")).thenReturn(Mono.just(pnServiceDeskOperations));
        Mockito.when(this.addressDAO.getAddress("opId1234")).thenReturn(Mono.just(new PnServiceDeskAddress()));
        Mockito.when(this.addressManagerClient.deduplicates(Mockito.any())).thenReturn(Mono.just(new DeduplicatesResponseDto()));
        Mockito.when(this.pnDeliveryPushClient.paperNotificationFailed(Mockito.any())).thenReturn(Flux.just(responsePaperNotificationFailedDtoDto));
        Mockito.when(this.operationDAO.updateEntity(Mockito.any())).thenReturn(Mono.empty());
        Assertions.assertThrows(PnGenericException.class, () ->
                this.validationOperationAction.execute("opId1234")
        );
    }

    @Test
    void executeGetNotificationLegalFactsPrivateEmpty() {
        PnGenericException exception = new PnGenericException(null, null);
        Mockito.when(this.operationDAO.getByOperationId("opId1234")).thenReturn(Mono.just(pnServiceDeskOperations));
        Mockito.when(this.addressDAO.getAddress("opId1234")).thenReturn(Mono.just(new PnServiceDeskAddress()));
        Mockito.when(this.addressManagerClient.deduplicates(Mockito.any())).thenReturn(Mono.just(new DeduplicatesResponseDto()));
        Mockito.when(this.pnDeliveryPushClient.paperNotificationFailed(Mockito.any())).thenReturn(Flux.just(responsePaperNotificationFailedDtoDto));
        Mockito.when(this.pnDeliveryClient.getSentNotificationPrivate(Mockito.any())).thenReturn(Mono.just(sentNotificationDto));
        Mockito.when(this.operationDAO.updateEntity(Mockito.any())).thenReturn(Mono.just(pnServiceDeskOperations));
        Mockito.when(this.pnDeliveryPushClient.getNotificationLegalFactsPrivate(Mockito.any(), Mockito.any())).thenThrow(exception);
        Mockito.when(this.operationDAO.searchOperationsFromRecipientInternalId(Mockito.anyString())).thenReturn(Flux.empty());

        Assertions.assertDoesNotThrow(() -> {
            this.validationOperationAction.execute("opId1234");
        });
    }

    @Test
    void executeGetNotificationLegalFactsPrivateEmptyAndIsAvailableFalse() {
        pnServiceDeskOperations.getAttachments().get(0).setIsAvailable(Boolean.FALSE);
        PnGenericException exception = new PnGenericException(null, null);
        Mockito.when(this.operationDAO.getByOperationId("opId1234")).thenReturn(Mono.just(pnServiceDeskOperations));
        Mockito.when(this.addressDAO.getAddress("opId1234")).thenReturn(Mono.just(new PnServiceDeskAddress()));
        Mockito.when(this.addressManagerClient.deduplicates(Mockito.any())).thenReturn(Mono.just(new DeduplicatesResponseDto()));
        Mockito.when(this.pnDeliveryPushClient.paperNotificationFailed(Mockito.any())).thenReturn(Flux.just(responsePaperNotificationFailedDtoDto));
        Mockito.when(this.pnDeliveryClient.getSentNotificationPrivate(Mockito.any())).thenReturn(Mono.just(sentNotificationDto));
        Mockito.when(this.operationDAO.updateEntity(Mockito.any())).thenReturn(Mono.just(pnServiceDeskOperations));
        Mockito.when(this.pnDeliveryPushClient.getNotificationLegalFactsPrivate(Mockito.any(), Mockito.any())).thenThrow(exception);
        Mockito.when(this.operationDAO.searchOperationsFromRecipientInternalId(Mockito.anyString())).thenReturn(Flux.empty());

        Assertions.assertDoesNotThrow(() -> {
            this.validationOperationAction.execute("opId1234");
        });
    }

    @Test
    void executeGetNotificationLegalFactsPrivate() {
        Mockito.when(this.operationDAO.getByOperationId("opId1234")).thenReturn(Mono.just(pnServiceDeskOperations));
        Mockito.when(this.addressDAO.getAddress("opId1234")).thenReturn(Mono.just(new PnServiceDeskAddress()));
        Mockito.when(this.addressManagerClient.deduplicates(Mockito.any())).thenReturn(Mono.just(new DeduplicatesResponseDto()));
        Mockito.when(this.pnDeliveryPushClient.paperNotificationFailed(Mockito.any())).thenReturn(Flux.just(responsePaperNotificationFailedDtoDto));
        Mockito.when(this.pnDeliveryClient.getSentNotificationPrivate(Mockito.any())).thenReturn(Mono.just(sentNotificationDto));
        Mockito.when(this.operationDAO.updateEntity(Mockito.any())).thenReturn(Mono.just(pnServiceDeskOperations));
        Mockito.when(this.pnDeliveryPushClient.getNotificationLegalFactsPrivate(Mockito.any(), Mockito.any())).thenReturn(Flux.just(legalFactListElementDto));
        Mockito.when(this.paperChannelClient.sendPaperPrepareRequest(Mockito.any(), Mockito.any())).thenReturn(Mono.just(paperChannelUpdateDto));
        Mockito.when(this.pnDataVaultClient.deAnonymized(Mockito.any())).thenReturn(Mono.just("MDDLOP3333-e"));
        Mockito.when(this.safeStorageClient.getFile(Mockito.any())).thenReturn(Mono.just(getFileDownloadResponse()));
        Mockito.when(this.operationDAO.searchOperationsFromRecipientInternalId(Mockito.anyString())).thenReturn(Flux.empty());

        this.validationOperationAction.execute("opId1234");


        ArgumentCaptor<PrepareRequestDto> capturePrepareRequest = ArgumentCaptor.forClass(PrepareRequestDto.class);
        Mockito.verify(paperChannelClient).sendPaperPrepareRequest(Mockito.any(), capturePrepareRequest.capture());

        Assertions.assertNotNull(capturePrepareRequest.getValue().getRequestId());
        Assertions.assertNotNull(capturePrepareRequest.getValue().getIun());
        Assertions.assertEquals("opId1234", capturePrepareRequest.getValue().getIun());
        Assertions.assertEquals("PF", capturePrepareRequest.getValue().getReceiverType());
        Assertions.assertEquals("SERVICE_DESK_OPID-opId1234", capturePrepareRequest.getValue().getRequestId());
        Assertions.assertEquals(ProposalTypeEnumDto.RS, capturePrepareRequest.getValue().getProposalProductType());

    }


    private DeduplicatesResponseDto getDeduplicatesResponseWithError() {
        DeduplicatesResponseDto dto = new DeduplicatesResponseDto();
        dto.setError("Invalid Address");
        dto.setCorrelationId("ABC");
        return dto;
    }

    private DeduplicatesResponseDto getDeduplicatesResponse(boolean equals) {
        DeduplicatesResponseDto dto = new DeduplicatesResponseDto();
        dto.setEqualityResult(equals);
        dto.setCorrelationId("ABC");
        return dto;
    }

    private FileDownloadResponse getFileDownloadResponse() {
        FileDownloadResponse response = new FileDownloadResponse();
        FileDownloadInfo info = new FileDownloadInfo();
        info.setUrl("http://localhost:8080/name.pdf");
        response.setDownload(info);
        response.setKey("123-FILE-KEY");
        return response;
    }


}