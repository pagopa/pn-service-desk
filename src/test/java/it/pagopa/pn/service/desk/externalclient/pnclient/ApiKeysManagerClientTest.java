package it.pagopa.pn.service.desk.externalclient.pnclient;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnapikeymanager.v1.dto.ApiKeyRowDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnapikeymanager.v1.dto.ApiKeyStatusDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnapikeymanager.v1.dto.ResponseApiKeysDto;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.apikeysmanager.ApiKeysManagerClientImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApiKeysManagerClientTest extends BaseTest.WithMockServer{

    @Autowired
    private ApiKeysManagerClientImpl apiKeysManagerClient;

    private final ResponseApiKeysDto expected = new ResponseApiKeysDto();

    @BeforeEach
    public void init() {
        List<ApiKeyRowDto> apiKeyRowDtoList = new ArrayList<>();

        ApiKeyRowDto apiKeyRowDto = new ApiKeyRowDto();
        apiKeyRowDto.setId("0055466d-9bb7-4b2c-96f1-df03f339ecb8");
        apiKeyRowDto.setName("Simona");
        apiKeyRowDto.setPdnd(false);
        List<String> groups = new ArrayList<>();
        Collections.addAll(groups, "000",
                "001<img src=x onerror=prompt(1)/>",
                "Carlotta",
                "Carlotta2");

        apiKeyRowDto.setGroups(groups);
        apiKeyRowDto.setStatus(ApiKeyStatusDto.ENABLED);

        apiKeyRowDtoList.add(apiKeyRowDto);

        expected.setItems(apiKeyRowDtoList);
        expected.setTotal(72);
    }

    @Test
    void getBoApiKeysTest(){
        ResponseApiKeysDto actual = apiKeysManagerClient
                .getBoApiKeys("5b994d4a-0fa8-47ac-9c7b-354f1d44a1ce")
                .block();

        ApiKeyRowDto expectedRow = expected.getItems().get(0);
        ApiKeyRowDto actualRow = actual.getItems().get(0);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expectedRow.getId(), actualRow.getId());
        Assertions.assertEquals(expectedRow.getName(), actualRow.getName());
        Assertions.assertEquals(expectedRow.getPdnd(), actualRow.getPdnd());
        Assertions.assertEquals(expectedRow.getGroups(), actualRow.getGroups());
        Assertions.assertEquals(expectedRow.getStatus(), actualRow.getStatus());
        Assertions.assertEquals(expected.getTotal(), actual.getTotal());
    }

}
