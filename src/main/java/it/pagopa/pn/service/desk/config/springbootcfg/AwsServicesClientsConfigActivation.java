package it.pagopa.pn.service.desk.config.springbootcfg;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClient;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import it.pagopa.pn.commons.configs.RuntimeMode;
import it.pagopa.pn.commons.configs.aws.AwsConfigs;
import it.pagopa.pn.commons.configs.aws.AwsServicesClientsConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class AwsServicesClientsConfigActivation extends AwsServicesClientsConfig {

    private final AwsConfigsActivation properties;

    public AwsServicesClientsConfigActivation(AwsConfigs props, AwsConfigsActivation properties) {
        super(props, RuntimeMode.PROD);
        this.properties = properties;
    }


    @Bean
    public AWSKMS kms() {
        final AWSKMSClientBuilder builder = AWSKMSClient.builder();

        if (Optional.ofNullable(properties.getKms().getEndpoint()).isPresent()) {
            builder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(properties.getKms().getEndpoint(), properties.getKms().getRegion()));
        } else {
            Optional.ofNullable(properties.getKms().getRegion()).ifPresent(builder::setRegion);
        }

        return builder.build();
    }

}
