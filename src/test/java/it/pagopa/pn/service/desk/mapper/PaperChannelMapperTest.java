package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.config.PnServiceDeskConfigs;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.AnalogAddressDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.PrepareEventDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.SendRequestDto;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaperChannelMapperTest  {

    private final PnServiceDeskOperations pnServiceDeskOperations= new PnServiceDeskOperations();
    private final PnServiceDeskAttachments pnServiceDeskAttachments= new PnServiceDeskAttachments();

    @BeforeEach
    public void inizialize(){
        pnServiceDeskAttachments.setIun("123");
        pnServiceDeskAttachments.setFilesKey(new ArrayList<>());

        pnServiceDeskOperations.setOperationId("123");
        pnServiceDeskOperations.setOperationStartDate(Instant.now());
        pnServiceDeskOperations.setOperationLastUpdateDate(Instant.now());
        pnServiceDeskOperations.setStatus("OK");
    }

    @Test
    void getPrepareRequest() {
        assertNotNull(PaperChannelMapper.getPrepareRequest(pnServiceDeskOperations(), pnServiceDeskAddress(), new ArrayList<>(), "1234", pnServiceDeskConfigs() ));
    }

    @Test
    void getPaperSendRequest() {
        assertNotNull(PaperChannelMapper.getPaperSendRequest(pnServiceDeskConfigs(), pnServiceDeskOperations(), prepareEventDto()));
    }

    @Test
    void toListStringAttachments() {
        pnServiceDeskAttachments.setIsAvailable(true);

        List<PnServiceDeskAttachments> attachments = new ArrayList<>();
        attachments.add(pnServiceDeskAttachments);

        pnServiceDeskOperations.setAttachments(attachments);

        SendRequestDto sendRequestDto= PaperChannelMapper.getPaperSendRequest(pnServiceDeskConfigs(), pnServiceDeskOperations, prepareEventDto());

        assertNotNull(sendRequestDto);
        assertNotNull(sendRequestDto.getAttachmentUrls());
    }

    PnServiceDeskConfigs pnServiceDeskConfigs() {
        PnServiceDeskConfigs.SenderAddress senderAddress= new PnServiceDeskConfigs.SenderAddress();
        senderAddress.setFullname("Mario Rossi");
        senderAddress.setAddress("Via Roma");
        senderAddress.setCity("Napoli");
        senderAddress.setPr("NA");
        senderAddress.setCountry("Italia");

        PnServiceDeskConfigs pnServiceDeskConfigs= new PnServiceDeskConfigs();
        pnServiceDeskConfigs.setSenderPaId("1234");
        pnServiceDeskConfigs.setSenderAddress(senderAddress);
        return pnServiceDeskConfigs;
    }

    PnServiceDeskOperations pnServiceDeskOperations() {
        PnServiceDeskAttachments pnServiceDeskAttachments= new PnServiceDeskAttachments();
        pnServiceDeskAttachments.setIun("iun");
        pnServiceDeskAttachments.setIsAvailable(false);

        List<PnServiceDeskAttachments> attachments= new ArrayList<>();
        attachments.add(pnServiceDeskAttachments);

        PnServiceDeskOperations pnServiceDeskOperations = new PnServiceDeskOperations();
        pnServiceDeskOperations.setOperationId("123");
        pnServiceDeskOperations.setOperationStartDate(Instant.now());
        pnServiceDeskOperations.setOperationLastUpdateDate(Instant.now());
        pnServiceDeskOperations.setStatus("OK");
        pnServiceDeskOperations.setRecipientInternalId("1234");
        pnServiceDeskOperations.setAttachments(attachments);
        return pnServiceDeskOperations;
    }

    PrepareEventDto prepareEventDto() {

        PrepareEventDto prepareEventDto= new PrepareEventDto();
        prepareEventDto.setReceiverAddress(new AnalogAddressDto());
        prepareEventDto.setRequestId("1234");
        prepareEventDto.setProductType("890");

        return prepareEventDto;
    }

    PnServiceDeskAddress pnServiceDeskAddress() {

        PnServiceDeskAddress address= new PnServiceDeskAddress();


        return address;
    }
}