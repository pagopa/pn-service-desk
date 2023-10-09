package it.pagopa.pn.service.desk.encryption.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EncrypytionModelTest {
    private EncryptionModel encryptionModel;
    private String keyId;
    private String algorithm;

    @BeforeEach
    void setUp(){
        this.initialize();
    }

    @Test
    void setGetTest() {
        encryptionModel = initZoneProductType();
        Assertions.assertNotNull(encryptionModel);

        String keyId = "keyId2";
        String algorithm = "algorithm2";

        encryptionModel.setKeyId(keyId);
        encryptionModel.setAlgorithm(algorithm);

        Assertions.assertEquals(keyId, encryptionModel.getKeyId());
        Assertions.assertEquals(algorithm, encryptionModel.getAlgorithm());
    }

    private EncryptionModel initZoneProductType() {
        return new EncryptionModel(keyId, algorithm);
    }

    private void initialize() {
        keyId = "keyId";
        algorithm = "algorithm";
    }
}
