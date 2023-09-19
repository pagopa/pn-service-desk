package it.pagopa.pn.service.desk.utility;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pndeliverypush.v1.dto.ResponsePaperNotificationFailedDtoDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.StatusCodeEnumDto;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAttachments;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Utility {
    private static final String REQUEST_ID_TEMPLATE = "SERVICE_DESK_OPID-";
    public static final String CONTENT_TYPE_VALUE = "application/octet-stream";
    public static final String SAFESTORAGE_BASE_URL = "safestorage://";

    private Utility(){}

    public static String generateRequestId(String operationId){
        return String.format(REQUEST_ID_TEMPLATE.concat("%s"), operationId);
    }

    public static String extractOperationId(String requestId){
        if (requestId.contains(REQUEST_ID_TEMPLATE)){
            return requestId.replace(REQUEST_ID_TEMPLATE, "");
        }
        return null;
    }

    public static String generateOperationId(@NotNull String prefix, String suffix){
        if (StringUtils.isBlank(suffix))
            suffix = "000";
        return prefix.concat(suffix);
    }

    public static OperationStatusEnum getOperationStatusFrom(StatusCodeEnumDto statusCodePaperChannel){
        return getStatusMapping().get(statusCodePaperChannel);
    }

    private static Map<StatusCodeEnumDto, OperationStatusEnum> getStatusMapping(){
        Map<StatusCodeEnumDto, OperationStatusEnum> status = new EnumMap<>(StatusCodeEnumDto.class);
        status.put(StatusCodeEnumDto.KO, OperationStatusEnum.KO);
        status.put(StatusCodeEnumDto.KOUNREACHABLE, OperationStatusEnum.KO);
        status.put(StatusCodeEnumDto.OK, OperationStatusEnum.OK);
        status.put(StatusCodeEnumDto.PROGRESS, OperationStatusEnum.PROGRESS);
        return status;
    }

    public static OffsetDateTime getOffsetDateTimeFromDate(Instant date) {
        return OffsetDateTime.ofInstant(date, ZoneOffset.UTC);
    }

    public static Mono<Boolean> operationContainsIun(List<PnServiceDeskOperations> operation, List<ResponsePaperNotificationFailedDtoDto> notifications){
        List<String> iuns = new ArrayList<>();
        notifications.forEach(notification -> iuns.add(notification.getIun()));

        if(operation!=null){
            for(PnServiceDeskOperations op : operation){
                for(PnServiceDeskAttachments attachments : op.getAttachments()){
                    for(String iun : iuns){
                        if(attachments.getIun().equals(iun) && op.getStatus().equals(OperationStatusEnum.KO.toString())){
                            return Mono.just(true);
                        }
                    }
                }
            }
        }
        return Mono.just(false);
    }

}
