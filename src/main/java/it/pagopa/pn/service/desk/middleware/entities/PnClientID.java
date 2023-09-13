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
    public static final String COL_CLIENT_ID = "clientId";
    public static final String COL_API_KEY = "apiKey";

    @Getter(onMethod = @__({@DynamoDbPartitionKey,@DynamoDbAttribute(COL_API_KEY)}))
    private String apiKey;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_CLIENT_ID)}))
    private String clientId;

}
