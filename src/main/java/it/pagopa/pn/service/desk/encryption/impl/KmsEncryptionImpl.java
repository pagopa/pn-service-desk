package it.pagopa.pn.service.desk.encryption.impl;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.DecryptResponse;
import software.amazon.awssdk.services.kms.model.EncryptRequest;
import it.pagopa.pn.service.desk.config.springbootcfg.AwsConfigsActivation;
import it.pagopa.pn.service.desk.encryption.DataEncryption;
import it.pagopa.pn.service.desk.encryption.EncryptedUtils;
import it.pagopa.pn.service.desk.encryption.model.EncryptionModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.kms.model.EncryptionAlgorithmSpec;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@Slf4j
@Service
public class KmsEncryptionImpl implements DataEncryption {

    @Autowired
    private KmsClient kms;
    @Autowired
    private AwsConfigsActivation awsProperties;

    @Override
    public String encode(String data) {
        if(StringUtils.isNotEmpty(data)) {

            final EncryptRequest encryptRequest = EncryptRequest.builder()
                    .keyId(this.awsProperties.getKms().getKeyId())
                    .plaintext(SdkBytes.fromByteArray(data.getBytes(StandardCharsets.UTF_8)))
                    .build();

            SdkBytes encryptedBytes = kms.encrypt(encryptRequest).ciphertextBlob();

            return extractString(encryptedBytes, false);
        } else {
            return data;
        }
    }

    @Override
    public String decode(String data) {
        if (StringUtils.isNotEmpty(data)) {
            final EncryptedUtils token = EncryptedUtils.parse(data);

            final EncryptionModel options = token.getModel();

            final String keyId = Optional.ofNullable(options.getKeyId())
                                   .orElse(awsProperties.getKms().getKeyId());

            final String algorithm = Optional.ofNullable(options.getAlgorithm())
                                       .orElse("SYMMETRIC_DEFAULT");

            final DecryptRequest.Builder builder = DecryptRequest.builder()
                                                           .ciphertextBlob(SdkBytes.fromByteArray(token.getCipherBytes().array()))
                                                           .encryptionContext(token.getEncryptionContext())
                                                           .keyId(keyId);

            builder.encryptionAlgorithm(EncryptionAlgorithmSpec.fromValue(algorithm));

            DecryptResponse decryptResponse = kms.decrypt(builder.build());

            byte[] bytes = decryptResponse.plaintext().asByteArray();

            return new String(bytes, StandardCharsets.UTF_8);

        } else {
            return data;
        }
    }

    private static String extractString(final SdkBytes bytes, boolean isText) {
        if (bytes != null) {
            byte[] raw = bytes.asByteArray();

            if (isText) {
                return new String(raw, StandardCharsets.UTF_8);
            }

            return Base64.getEncoder().encodeToString(raw);
        } else {
            return "";
        }
    }

}