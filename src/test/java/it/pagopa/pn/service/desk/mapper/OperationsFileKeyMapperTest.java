package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.generated.openapi.msclient.safestorage.model.FileCreationResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class OperationsFileKeyMapperTest {


    @Test
    void testNotNull(){

    }

    @Test
    void getOperationFileKey() {
        assertNotNull(OperationsFileKeyMapper.getOperationFileKey("1234", "aboperation"));
    }

    @Test
    void getVideoUpload() {
        FileCreationResponse fileCreationResponse= new FileCreationResponse();
        fileCreationResponse.setSecret("secret");
        fileCreationResponse.setKey("1234");
        fileCreationResponse.setUploadUrl("url");
        assertNotNull(OperationsFileKeyMapper.getVideoUpload(fileCreationResponse));

    }
}
