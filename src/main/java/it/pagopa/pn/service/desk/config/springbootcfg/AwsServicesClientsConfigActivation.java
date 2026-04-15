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

        String endpoint = properties.getKms().getEndpoint();
        String region = properties.getKms().getRegion();

        boolean hasEndpoint = endpoint != null && !endpoint.isBlank();
        boolean hasRegion = region != null && !region.isBlank();

        if (hasEndpoint) {
            if (!hasRegion) {
                throw new IllegalStateException("KMS region must be set when using a custom endpoint");
            }
            builder.endpointOverride(URI.create(endpoint));
            builder.region(Region.of(region));
        } else if (hasRegion) {
            builder.region(Region.of(region));
        }

        return builder.build();
    }

}