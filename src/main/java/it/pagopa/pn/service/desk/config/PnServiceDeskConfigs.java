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


    private String safeStorageBasePath;
    private String dataVaultBasePath;
    private String raddFsuBasePath;
    private String addressManagerBasePath;
    private String deliveryPushBasePath;
    private String deliveryBasePath;
    private String paperChannelBasePath;
    private String safeStorageCxId;
    private String paperChannelCxId;
    private String safeStorageDocumentType;
    private Topics topics;
    private SenderAddress senderAddress;
    private String addressManagerCxId;
    private String addressManagerApiKey;
    private Long ttlReceiverAddress;
    private String productType;
    private String senderPaId;

    @Data
    public static class Topics {
        private String internalQueue;
        private String safeStorageEvents;
        private String paperChannelQueue;
    }

    @Data
    public static class SenderAddress {
        private String fullname;
        private String address;
        private String zipcode;
        private String city;
        private String pr;
        private String country;
    }

}
