package it.pagopa.pn.service.desk.middleware.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
@Getter
@Setter
@ToString
@NoArgsConstructor
public class PnServiceDeskAddress {

    public static final String COL_OPERATION_ID = "operationId";
    public static final String COL_FULL_NAME = "fullName";
    public static final String COL_NAME_RAW_2 = "nameRaw2";
    public static final String COL_ADDRESS = "address";
    public static final String COL_ADDRESS_RAW_2 = "addressRaw2";
    public static final String COL_CAP = "cap";
    public static final String COL_CITY = "city";
    public static final String COL_CITY_2 = "city2";
    public static final String COL_COUNTRY = "country";
    public static final String COL_PR = "pr";
    public static final String COL_TTL = "ttl";

    @Getter(onMethod = @__({@DynamoDbPartitionKey,@DynamoDbAttribute(COL_OPERATION_ID)}))
    private String operationId;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_FULL_NAME)}))
    private String fullName;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_NAME_RAW_2)}))
    private String nameRaw2;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_ADDRESS)}))
    private String address;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_ADDRESS_RAW_2)}))
    private String addressRaw2;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_CAP)}))
    private String cap;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_CITY)}))
    private String city;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_CITY_2)}))
    private String city2;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_COUNTRY)}))
    private String country;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_PR)}))
    private String pr;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_TTL)}))
    private Long ttl;

}
