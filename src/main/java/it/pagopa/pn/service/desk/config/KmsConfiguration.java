package it.pagopa.pn.service.desk.config;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClient;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import it.pagopa.pn.service.desk.encryption.DataEncryption;
import it.pagopa.pn.service.desk.encryption.impl.KmsEncryptionImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class KmsConfiguration {

    private final AwsKmsProperties properties;


    public KmsConfiguration(AwsKmsProperties properties) {
        this.properties = properties;
    }

    @Bean
    public AWSKMS kms() {
        final AWSKMSClientBuilder builder = AWSKMSClient.builder();

        if (Optional.ofNullable(properties.getEndpoint()).isPresent()) {
            builder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(properties.getEndpoint(), properties.getRegion()));
        } else {
            Optional.ofNullable(properties.getRegion()).ifPresent(builder::setRegion);
        }

        return builder.build();
    }

    @Bean
    @Qualifier("kmsEncryption")
    public DataEncryption kmsEncryption(AWSKMS awskms){
        return new KmsEncryptionImpl(awskms, this.properties);
    }
}
