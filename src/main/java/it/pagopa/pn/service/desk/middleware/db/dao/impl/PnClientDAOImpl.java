package it.pagopa.pn.service.desk.middleware.db.dao.impl;

import it.pagopa.pn.service.desk.config.springbootcfg.AwsConfigsActivation;
import it.pagopa.pn.service.desk.middleware.db.dao.PnClientDAO;
import it.pagopa.pn.service.desk.middleware.db.dao.common.BaseDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnClientID;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

@Repository
public class PnClientDAOImpl extends BaseDAO<PnClientID> implements PnClientDAO {


    public PnClientDAOImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient, DynamoDbAsyncClient dynamoDbAsyncClient, AwsConfigsActivation awsPropertiesConfig) {
        super(dynamoDbEnhancedAsyncClient, dynamoDbAsyncClient,
                awsPropertiesConfig.getDynamodbClientTable(), PnClientID.class);
    }

    @Override
    public Mono<PnClientID> getByApiKey(String apiKey) {
        return Mono.fromFuture(super.get(apiKey, null).thenApply(item -> item));
    }

    @Override
    public Mono<PnClientID> getByPrefix(String prefixValue) {
        return super.getBySecondaryIndex(PnClientID.INDEX_CLIENT_ID, prefixValue, null)
                .collectList()
                .flatMap(item -> {
                    if (item == null || item.isEmpty()){
                        return Mono.empty();
                    }
                    return Mono.just(item.get(0));
                });
    }
}
