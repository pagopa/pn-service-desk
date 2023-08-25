package it.pagopa.pn.service.desk.utility;

import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;

public class Utility {
    private static final String REQUEST_ID_TEMPLATE = "SERVICE_DESK_OPID-";


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

}
