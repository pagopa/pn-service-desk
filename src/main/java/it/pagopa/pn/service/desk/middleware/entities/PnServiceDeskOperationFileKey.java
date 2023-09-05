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
public class PnServiceDeskOperationFileKey {

    public static final String COL_FILE_KEY = "fileKey";
    public static final String COL_OPERATION_ID = "operationId";
    public static final String OPERATION_ID_INDEX = "operationId-index";

    @Getter(onMethod = @__({@DynamoDbPartitionKey,@DynamoDbAttribute(COL_FILE_KEY)}))
    private String fileKey;

    @Getter(onMethod = @__({@DynamoDbSecondaryPartitionKey(indexNames = OPERATION_ID_INDEX),@DynamoDbAttribute(COL_OPERATION_ID)}))
    private String operationId;
}
