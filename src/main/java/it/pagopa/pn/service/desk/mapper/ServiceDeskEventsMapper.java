package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskEvents;

import java.time.Instant;

public class ServiceDeskEventsMapper {

    private ServiceDeskEventsMapper(){}

    public static PnServiceDeskEvents toEntity(String statusDetail, String statusDescription){
        PnServiceDeskEvents pnServiceDeskEvents = new PnServiceDeskEvents();
        pnServiceDeskEvents.setStatusCode(statusDetail);
        pnServiceDeskEvents.setStatusDescription(statusDescription);
        pnServiceDeskEvents.setTimestamp(Instant.now());
        return pnServiceDeskEvents;
    }

}
