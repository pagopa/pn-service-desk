package it.pagopa.pn.service.desk.middleware.db.dao.impl;


import it.pagopa.pn.service.desk.config.springbootcfg.AwsConfigsActivation;
import it.pagopa.pn.service.desk.encryption.DataEncryption;
import it.pagopa.pn.service.desk.middleware.db.dao.AddressDAO;
import it.pagopa.pn.service.desk.middleware.db.dao.common.BaseDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
@Service
public class AddressDAOImpl extends BaseDAO<PnServiceDeskAddress> implements AddressDAO {
    public AddressDAOImpl(DataEncryption kmsEncryption,
                          DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                          DynamoDbAsyncClient dynamoDbAsyncClient,
                          AwsConfigsActivation awsPropertiesConfig) {
        super(kmsEncryption, dynamoDbEnhancedAsyncClient, dynamoDbAsyncClient,
                awsPropertiesConfig.getDynamodbAddressTable(), PnServiceDeskAddress.class);
    }

    @Override
    public Mono<PnServiceDeskAddress> createAddress(PnServiceDeskAddress address) {
        return Mono.fromFuture(super.put(address));
    }

    @Override
    public Mono<PnServiceDeskAddress> getAddress(String operationId) {
        return Mono.fromFuture(super.get(operationId, null).thenApply(item -> item));
    }

}
