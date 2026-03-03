package it.pagopa.pn.service.desk.middleware.db.dao.impl;

import it.pagopa.pn.service.desk.config.springbootcfg.AwsConfigsActivation;
import it.pagopa.pn.service.desk.encryption.DataEncryption;
import it.pagopa.pn.service.desk.middleware.db.dao.SubOperationDAO;
import it.pagopa.pn.service.desk.middleware.db.dao.common.BaseDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskSubOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactPutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

@Service
public class SubOperationDAOImpl extends BaseDAO<PnServiceDeskSubOperations> implements SubOperationDAO {

    protected SubOperationDAOImpl(DataEncryption kmsEncryption,
                                  DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                                  DynamoDbAsyncClient dynamoDbAsyncClient,
                                  AwsConfigsActivation awsPropertiesConfig) {
        super(kmsEncryption, dynamoDbEnhancedAsyncClient, dynamoDbAsyncClient,
                awsPropertiesConfig.getDynamodbOperationsTable(), PnServiceDeskSubOperations.class);
    }

    @Override
    public Mono<PnServiceDeskSubOperations> getByOperationId(String operationId) {
        return Mono.fromFuture(this.get(operationId, null).thenApply(item -> item));
    }

    @Override
    public Mono<PnServiceDeskSubOperations> updateEntity(PnServiceDeskSubOperations subOperation) {
        return Mono.fromFuture(super.update(subOperation));
    }

    @Override
    public void createTransaction(TransactWriteItemsEnhancedRequest.Builder builder,
                                  PnServiceDeskSubOperations subOperation) {
        TransactPutItemEnhancedRequest<PnServiceDeskSubOperations> requestEntity =
                TransactPutItemEnhancedRequest.builder(PnServiceDeskSubOperations.class)
                        .item(subOperation)
                        .build();
        builder.addPutItem(this.dynamoTable, requestEntity);
    }
}
