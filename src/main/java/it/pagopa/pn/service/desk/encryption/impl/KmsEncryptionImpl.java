package it.pagopa.pn.service.desk.encryption.impl;

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;
import com.amazonaws.services.kms.model.EncryptRequest;
import it.pagopa.pn.service.desk.config.AwsKmsProperties;
import it.pagopa.pn.service.desk.encryption.DataEncryption;
import it.pagopa.pn.service.desk.encryption.EncryptedUtils;
import it.pagopa.pn.service.desk.encryption.model.EncryptionModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.apache.commons.lang3.StringUtils;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;


@Slf4j
@Qualifier("kmsEncryption")
public class KmsEncryptionImpl implements DataEncryption {

    private final AWSKMS kms;
    private final AwsKmsProperties awsKmsProperties;

    public KmsEncryptionImpl(AWSKMS awskms, AwsKmsProperties awsKmsProperties) {
        this.kms = awskms;
        this.awsKmsProperties = awsKmsProperties;
    }

    @Override
    public String encode(String data) {
        if(StringUtils.isNotEmpty(data)) {

            final EncryptRequest encryptRequest = new EncryptRequest()
                    .withKeyId(this.awsKmsProperties.getKeyId())
                    .withPlaintext(ByteBuffer.wrap(data.getBytes(StandardCharsets.UTF_8)));

            final ByteBuffer encryptedBytes = kms.encrypt(encryptRequest).getCiphertextBlob();

            return extractString(encryptedBytes, false);
        } else {
            return data;
        }
    }

    @Override
    public String decode(String data) {
        if(StringUtils.isNotEmpty(data)) {
            final EncryptedUtils token = EncryptedUtils.parse(data);

            final DecryptRequest decryptRequest = new DecryptRequest()
                    .withCiphertextBlob(token.getCipherBytes())
                    .withEncryptionContext(token.getEncryptionContext());

            final EncryptionModel options = token.getModel();
            final String keyId = Optional.ofNullable(options.getKeyId()).orElse(awsKmsProperties.getKeyId());
            final String algorithm = Optional.ofNullable(options.getAlgorithm()).orElse("SYMMETRIC_DEFAULT");
            decryptRequest.setEncryptionAlgorithm(algorithm);
            decryptRequest.setKeyId(keyId);

            DecryptResult decryptResult = kms.decrypt(decryptRequest);
            byte [] bytes = decryptResult.getPlaintext().array();
            return new String(bytes, StandardCharsets.UTF_8);
        } else {
            return data;
        }
    }


    private static String extractString(final ByteBuffer bb, boolean isText) {
        if (bb.hasRemaining()) {
            final byte[] bytes = new byte[bb.remaining()];
            bb.get(bytes, bb.arrayOffset(), bb.remaining());
            if (isText)
                return new String(bytes, StandardCharsets.UTF_8);

            return Base64.getEncoder().encodeToString(bytes);
        } else {
            return "";
        }
    }
}
