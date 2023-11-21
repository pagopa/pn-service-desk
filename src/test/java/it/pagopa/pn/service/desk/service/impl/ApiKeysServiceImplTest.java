package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.config.BaseTest;
import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnapikeymanager.v1.dto.ApiKeyRowDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnapikeymanager.v1.dto.ApiKeyStatusDto;
import it.pagopa.pn.service.desk.generated.openapi.msclient.pnapikeymanager.v1.dto.ResponseApiKeysDto;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.ApiKeyRow;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.ResponseApiKeys;
import it.pagopa.pn.service.desk.middleware.externalclient.pnclient.apikeysmanager.ApiKeysManagerClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.ERROR_ON_KEYS_MANAGER_CLIENT;

class ApiKeysServiceImplTest extends BaseTest {

    @MockBean
    private ApiKeysManagerClient apiKeysManagerClient;
    @Autowired
    private ApiKeysServiceImpl apiKeysService;

    private final ResponseApiKeysDto responseApiKeysDto = new ResponseApiKeysDto();

    @BeforeEach
    public void init() {
        List<ApiKeyRowDto> items = new ArrayList<>();
        ApiKeyRowDto apiKeyRowDto = new ApiKeyRowDto();
        apiKeyRowDto.setId("0055466d-9bb7-4b2c-96f1-df03f339ecb8");
        apiKeyRowDto.setName("Simona");
        apiKeyRowDto.setPdnd(false);
        apiKeyRowDto.setGroups(Arrays.asList(
                "000",
                "001<img src=x onerror=prompt(1)/>",
                "Carlotta",
                "Carlotta2"
        ));
        apiKeyRowDto.setStatus(ApiKeyStatusDto.ENABLED);
        items.add(apiKeyRowDto);

        responseApiKeysDto.setItems(items);
        responseApiKeysDto.setTotal(72);
    }

    @Test
    void getApiKeys() {
        Mockito.when(this.apiKeysManagerClient.getBoApiKeys(Mockito.any()))
                .thenReturn(Mono.just(responseApiKeysDto));

        ResponseApiKeys actual = this.apiKeysService.getApiKeys("5b994d4a-0fa8-47ac-9c7b-354f1d44a1ce")
                .block();

        Assertions.assertNotNull(actual);

        ApiKeyRowDto expectedItem = responseApiKeysDto.getItems().get(0);
        ApiKeyRow actualItem = actual.getItems().get(0);

        Assertions.assertEquals(expectedItem.getId(), actualItem.getId());
        Assertions.assertEquals(expectedItem.getName(), actualItem.getName());
        Assertions.assertEquals(expectedItem.getPdnd(), actualItem.getPdnd());
        Assertions.assertEquals(expectedItem.getGroups(), actualItem.getGroups());
        Assertions.assertEquals(expectedItem.getStatus().getValue(), actualItem.getStatus().getValue());
        Assertions.assertEquals(responseApiKeysDto.getTotal(), actual.getTotal());
    }

    @Test
    void getApiKeysApiKeysManagerClientError(){
        PnGenericException pnGenericException = new PnGenericException(ERROR_ON_KEYS_MANAGER_CLIENT, ERROR_ON_KEYS_MANAGER_CLIENT.getMessage());
        Mockito.when(this.apiKeysManagerClient.getBoApiKeys(Mockito.any()))
                .thenReturn(Mono.error(pnGenericException));

        StepVerifier.create(this.apiKeysService.getApiKeys("5b994d4a-0fa8-47ac-9c7b-354f1d44a1ce"))
                .expectError(PnGenericException.class)
                .verify();
    }

}