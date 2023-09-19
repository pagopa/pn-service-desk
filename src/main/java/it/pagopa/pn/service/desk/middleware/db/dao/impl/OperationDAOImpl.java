package it.pagopa.pn.service.desk.middleware.db.dao.impl;

import it.pagopa.pn.service.desk.config.springbootcfg.AwsConfigsActivation;
import it.pagopa.pn.service.desk.encryption.DataEncryption;
import it.pagopa.pn.service.desk.middleware.db.dao.AddressDAO;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.db.dao.common.BaseDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskAddress;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactPutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

@Service
public class OperationDAOImpl extends BaseDAO<PnServiceDeskOperations> implements OperationDAO {
    private final AddressDAO addressDAO;

    protected OperationDAOImpl(DataEncryption kmsEncryption,
                               DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                               DynamoDbAsyncClient dynamoDbAsyncClient, AddressDAO addressDAO,
                               AwsConfigsActivation awsPropertiesConfig) {
        super(kmsEncryption, dynamoDbEnhancedAsyncClient, dynamoDbAsyncClient,
                awsPropertiesConfig.getDynamodbOperationsTable(), PnServiceDeskOperations.class);
        this.addressDAO = addressDAO;
    }

    @Override
    public Mono<Tuple2<PnServiceDeskOperations, PnServiceDeskAddress>> createOperationAndAddress(PnServiceDeskOperations operations, PnServiceDeskAddress address) {
        TransactWriteItemsEnhancedRequest.Builder builder =
                TransactWriteItemsEnhancedRequest.builder();
        this.createTransaction(builder, operations);
        this.addressDAO.createWithTransaction(builder, address);
        return Mono.fromFuture(super.putWithTransact(builder.build()).thenApply(response -> Tuples.of(operations, address)));
    }

    @Override
    public Flux<PnServiceDeskOperations> searchOperationsFromRecipientInternalId(String taxId) {
        return this.getBySecondaryIndex(PnServiceDeskOperations.RECIPIENT_INTERNAL_INDEX, taxId, null);
    }

    @Override
    public Mono<PnServiceDeskOperations> getByOperationId(String operationId) {
        return Mono.fromFuture(this.get(operationId, null).thenApply(item -> item));
    }

    @Override
    public Mono<PnServiceDeskOperations> updateEntity(PnServiceDeskOperations operations) {
        return Mono.fromFuture(super.update(operations));
    }

    public Mono<PnServiceDeskOperations> updateEntityTransactional(PnServiceDeskOperations operations) {
        return Mono.fromFuture(super.update(operations));
    }

    private void createTransaction(TransactWriteItemsEnhancedRequest.Builder builder, PnServiceDeskOperations serviceDeskOperations) {
        TransactPutItemEnhancedRequest<PnServiceDeskOperations> requestEntity =
                TransactPutItemEnhancedRequest.builder(PnServiceDeskOperations.class)
                        .item(serviceDeskOperations)
                        .build();

        builder.addPutItem(this.dynamoTable, requestEntity);
    }
}
