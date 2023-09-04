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
    public OperationsFileKeyDAOImpl(DataEncryption kmsEncryption,
                                       DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                                       DynamoDbAsyncClient dynamoDbAsyncClient,
                                       AwsConfigsActivation awsPropertiesConfig) {
        super(kmsEncryption, dynamoDbEnhancedAsyncClient, dynamoDbAsyncClient,
                awsPropertiesConfig.getDynamodbFileKeyTable(), PnServiceDeskOperationFileKey.class);
    }

    @Override
    public Mono<PnServiceDeskOperationFileKey> updateVideoFileKey(PnServiceDeskOperationFileKey operationFileKey) {
        return Mono.fromFuture(super.put(operationFileKey));
    }

    @Override
    public Mono<PnServiceDeskOperationFileKey> getOperationFileKey(String key) {
        return Mono.fromFuture(super.get(key,null).thenApply(item -> item));
    }

    @Override
    public Mono<PnServiceDeskOperationFileKey> getFileKeyByOperationId(String operationId) {
        return this.getBySecondaryIndex(PnServiceDeskOperationFileKey.OPERATION_ID_INDEX, operationId, null)
                .collectList()
                .flatMap(item -> {
                    if (item.isEmpty()) return Mono.empty();
                    return Mono.just(item.get(0));
                });
    }
}
