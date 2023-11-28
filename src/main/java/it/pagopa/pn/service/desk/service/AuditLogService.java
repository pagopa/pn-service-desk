package it.pagopa.pn.service.desk.service;

import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;

public interface AuditLogService {

    PnAuditLogEvent buildAuditLogEvent(String iun, PnAuditLogEventType pnAuditLogEventType, String message, Object ... arguments);
    PnAuditLogEvent buildAuditLogEvent(PnAuditLogEventType pnAuditLogEventType, String message, Object ... arguments);
}
