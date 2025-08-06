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
    public static final String COL_NAME_ROW_2 = "nameRow2";
    public static final String COL_ADDRESS = "address";
    public static final String COL_ADDRESS_ROW_2 = "addressRow2";
    public static final String COL_CAP = "cap";
    public static final String COL_CITY = "city";
    public static final String COL_CITY_2 = "city2";
    public static final String COL_COUNTRY = "country";
    public static final String COL_PR = "pr";
    public static final String COL_TTL = "ttl";
    public static final String COL_TYPE = "type";

    @Getter(onMethod = @__({@DynamoDbPartitionKey,@DynamoDbAttribute(COL_OPERATION_ID)}))
    private String operationId;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_FULL_NAME)}))
    @ToString.Exclude
    private String fullName;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_NAME_ROW_2)}))
    @ToString.Exclude
    private String nameRow2;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_ADDRESS)}))
    @ToString.Exclude
    private String address;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_ADDRESS_ROW_2)}))
    @ToString.Exclude
    private String addressRow2;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_CAP)}))
    @ToString.Exclude
    private String cap;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_CITY)}))
    @ToString.Exclude
    private String city;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_CITY_2)}))
    @ToString.Exclude
    private String city2;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_COUNTRY)}))
    @ToString.Exclude
    private String country;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_PR)}))
    @ToString.Exclude
    private String pr;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_TTL)}))
    private Long ttl;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_TYPE)}))
    private String type;

}
