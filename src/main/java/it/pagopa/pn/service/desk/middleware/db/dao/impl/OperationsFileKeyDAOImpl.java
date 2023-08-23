package it.pagopa.pn.service.desk.middleware.db.dao.impl;

import it.pagopa.pn.service.desk.config.springbootcfg.AwsConfigsActivation;
import it.pagopa.pn.service.desk.encryption.DataEncryption;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationsFileKeyDAO;
import it.pagopa.pn.service.desk.middleware.db.dao.common.BaseDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperationFileKey;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

@Service
public class OperationsFileKeyDAOImpl extends BaseDAO<PnServiceDeskOperationFileKey> implements OperationsFileKeyDAO {
    protected OperationsFileKeyDAOImpl(DataEncryption kmsEncryption,
                                       DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                                       DynamoDbAsyncClient dynamoDbAsyncClient,
                                       AwsConfigsActivation awsPropertiesConfig) {
        super(kmsEncryption, dynamoDbEnhancedAsyncClient, dynamoDbAsyncClient,
                awsPropertiesConfig.getDynamodbAddressTable(), PnServiceDeskOperationFileKey.class);
    }

    @Override
    public Mono<PnServiceDeskOperationFileKey> updateVideoFileKey(PnServiceDeskOperationFileKey operationFileKey) {
        return Mono.fromFuture(super.put(operationFileKey));
    }
}
