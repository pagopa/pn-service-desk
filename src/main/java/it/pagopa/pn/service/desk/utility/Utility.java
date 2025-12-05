package it.pagopa.pn.service.desk.utility;

import it.pagopa.pn.service.desk.generated.openapi.msclient.pnexternalchannel.v1.dto.ProgressEventCategoryDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnpaperchannel.v1.dto.StatusCodeEnumDto;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import org.apache.commons.lang3.StringUtils;

import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Locale;
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

public static OperationStatusEnum getEcOperationStatusFrom(ProgressEventCategoryDto statusCodeExternalChannel){
        return getEcStatusMapping().get(statusCodeExternalChannel);
}

    private static Map<StatusCodeEnumDto, OperationStatusEnum> getStatusMapping(){
        Map<StatusCodeEnumDto, OperationStatusEnum> status = new EnumMap<>(StatusCodeEnumDto.class);
        status.put(StatusCodeEnumDto.KO, OperationStatusEnum.KO);
        status.put(StatusCodeEnumDto.KOUNREACHABLE, OperationStatusEnum.KO);
        status.put(StatusCodeEnumDto.OK, OperationStatusEnum.OK);
        status.put(StatusCodeEnumDto.PROGRESS, OperationStatusEnum.PROGRESS);
        return status;
    }

    private static Map<ProgressEventCategoryDto, OperationStatusEnum> getEcStatusMapping(){
        Map<ProgressEventCategoryDto, OperationStatusEnum> status = new EnumMap<>(ProgressEventCategoryDto.class);
        status.put(ProgressEventCategoryDto.ERROR, OperationStatusEnum.KO);
        status.put(ProgressEventCategoryDto.OK, OperationStatusEnum.OK);
        status.put(ProgressEventCategoryDto.PROGRESS, OperationStatusEnum.PROGRESS);
        return status;
    }

    public static OffsetDateTime getOffsetDateTimeFromDate(Instant date) {
        return OffsetDateTime.ofInstant(date, ZoneOffset.UTC);
    }

    public static String cleanUpOperationId (String operationId){
        int removeString = operationId.indexOf(Const.OPERATION_ID_SUFFIX);
        String cleanUpOperationId;
        if (removeString != -1) {
            cleanUpOperationId = operationId.substring(0, removeString);
        }else {
            cleanUpOperationId = operationId;
        }
        return cleanUpOperationId;
    }

    public static String extractFileKeyFromUrl(String url) {
        if (url != null) {
            URI uri = URI.create(url);
            String path = uri.getPath();
            return path.substring(path.lastIndexOf('/') + 1);
        }
        return null;
    }

    public static String formatDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                                                       .withLocale(Locale.ITALY);
        return LocalDate.parse(date).format(formatter);
    }

}
