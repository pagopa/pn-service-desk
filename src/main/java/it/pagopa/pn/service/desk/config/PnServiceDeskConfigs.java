package it.pagopa.pn.service.desk.config;

import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConfigurationProperties( prefix = "pn.service-desk")
@Data
@Import({SharedAutoConfiguration.class})
public class PnServiceDeskConfigs {


    private String safeStorageBaseUrl;
    private String dataVaultBaseUrl;
    private String raddFsuBaseUrl;
    private String safeStorageCxId;
    private Topics topics;


    @Data
    public static class Topics {
        private String internalQueue;
        private String safeStorageEvents;
    }


}
