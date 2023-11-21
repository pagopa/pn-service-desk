package it.pagopa.pn.service.desk.mapper;


import it.pagopa.pn.service.desk.generated.openapi.msclient.pnapikeymanager.v1.dto.ResponseApiKeysDto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.ApiKeyRow;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.ApiKeyStatus;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.ResponseApiKeys;

import java.util.ArrayList;
import java.util.List;

public class ApiKeysMapper {

    public static ResponseApiKeys responseApiKeys(ResponseApiKeysDto responseApiKeysDto){
        ResponseApiKeys responseApiKeys = new ResponseApiKeys();

        List<ApiKeyRow> apiKeyRowList = new ArrayList<>();
        responseApiKeysDto.getItems().forEach(apiKeyRowDto -> {
            ApiKeyRow apiKeyRow = new ApiKeyRow();
            apiKeyRow.setName(apiKeyRowDto.getName());
            apiKeyRow.setId(apiKeyRowDto.getId());
            apiKeyRow.setStatus(ApiKeyStatus.fromValue(apiKeyRowDto.getStatus().getValue()));
            apiKeyRow.setGroups(apiKeyRowDto.getGroups());
            apiKeyRow.setPdnd(apiKeyRowDto.getPdnd());
            apiKeyRowList.add(apiKeyRow);
        });

        responseApiKeys.setItems(apiKeyRowList);
        responseApiKeys.setTotal(responseApiKeysDto.getTotal());

        return responseApiKeys;
    }

}