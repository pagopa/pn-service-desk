package it.pagopa.pn.service.desk.middleware.db.dao.impl;

import it.pagopa.pn.service.desk.config.springbootcfg.AwsConfigsActivation;
import it.pagopa.pn.service.desk.encryption.DataEncryption;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.db.dao.common.BaseDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

@Service
public class OperationDAOImpl extends BaseDAO<PnServiceDeskOperations> implements OperationDAO {
    protected OperationDAOImpl(DataEncryption kmsEncryption,
                               DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                               DynamoDbAsyncClient dynamoDbAsyncClient,
                               AwsConfigsActivation awsPropertiesConfig) {
        super(kmsEncryption, dynamoDbEnhancedAsyncClient, dynamoDbAsyncClient,
                awsPropertiesConfig.getDynamodbOperationsTable(), PnServiceDeskOperations.class);
    }

    @Override
    public Mono<PnServiceDeskOperations> createOperation(PnServiceDeskOperations operations) {
        return Mono.fromFuture(super.put(operations));
    }

    @Override
    public Flux<PnServiceDeskOperations> searchOperationsFromRecipientInternalId(String taxId) {
        QueryConditional conditional = CONDITION_EQUAL_TO.apply(keyBuild(taxId, null));
        return this.getBySecondaryIndex("recipientInternalId-index", taxId, null);
    }

    @Override
    public Mono<PnServiceDeskOperations> getByOperationId(String operationId) {
        return Mono.fromFuture(this.get(operationId, null).thenApply(item -> item));
    }
}
