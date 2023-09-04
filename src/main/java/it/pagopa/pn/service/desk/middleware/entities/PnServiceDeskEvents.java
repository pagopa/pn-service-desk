package it.pagopa.pn.service.desk.middleware.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.time.Instant;

@DynamoDbBean
@Getter
@Setter
@ToString
public class PnServiceDeskEvents {

public static final String COL_STATUS_CODE = "statusCode";
public static final String COL_STATUS_DESCRIPTION = "statusDescription";
public static final String COL_STATUS_TIMESTAMP = "timestamp";

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_STATUS_CODE)}))
    private String statusCode;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_STATUS_DESCRIPTION)}))
    private String statusDescription;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_STATUS_TIMESTAMP)}))
    private Instant timestamp;
}
