package it.pagopa.pn.service.desk.middleware.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

@DynamoDbBean
@Getter
@Setter
@ToString
@NoArgsConstructor
public class PnClientID {
    public static final String COL_CLIENT_ID = "clientId"; //clientId
    public static final String COL_API_KEY = "apiKkey";//api key (zen-desk)
    public static final String INDEX_CLIENT_ID = "client-id-index";

    @Getter(onMethod = @__({@DynamoDbPartitionKey,@DynamoDbAttribute(COL_API_KEY)}))
    private String apiKkey;

    @Getter(onMethod = @__({@DynamoDbSecondaryPartitionKey(indexNames = INDEX_CLIENT_ID), @DynamoDbAttribute(COL_CLIENT_ID)}))
    private String clientId;

}
