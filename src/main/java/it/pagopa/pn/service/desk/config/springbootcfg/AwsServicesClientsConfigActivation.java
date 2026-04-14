package it.pagopa.pn.service.desk.config.springbootcfg;

import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.KmsClientBuilder;
import software.amazon.awssdk.regions.Region;
import it.pagopa.pn.commons.configs.RuntimeMode;
import it.pagopa.pn.commons.configs.aws.AwsConfigs;
import it.pagopa.pn.commons.configs.aws.AwsServicesClientsConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.util.Optional;

@Configuration
public class AwsServicesClientsConfigActivation extends AwsServicesClientsConfig {

    private final AwsConfigsActivation properties;

    public AwsServicesClientsConfigActivation(AwsConfigs props, AwsConfigsActivation properties) {
        super(props, RuntimeMode.PROD);
        this.properties = properties;
    }


    @Bean
    public KmsClient kms() {
        KmsClientBuilder builder = KmsClient.builder();

        if (Optional.ofNullable(properties.getKms().getEndpoint()).isPresent()) {
            builder.endpointOverride(URI.create(properties.getKms().getEndpoint()));
            builder.region(Region.of(properties.getKms().getRegion()));
        } else {
            Optional.ofNullable(properties.getKms().getRegion())
                    .ifPresent(r -> builder.region(Region.of(r)));
        }

        return builder.build();
    }

}