package it.pagopa.pn.service.desk.config.springbootcfg;

import it.pagopa.pn.commons.configs.aws.AwsConfigs;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("aws")
@Getter
@Setter
public class AwsConfigsActivation extends AwsConfigs {

    private String dynamodbOperationsTable;
    private String dynamodbAddressTable;
    private String dynamodbFileKeyTable;
    private Kms kms;
    @Data
    public static class Kms {
        private String keyId;
        private String endpoint;
        private String region;
    }
}
