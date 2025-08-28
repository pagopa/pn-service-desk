package it.pagopa.pn.service.desk.middleware.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

import java.time.Instant;
import java.util.List;

@DynamoDbBean
@Getter
@Setter
@ToString
@NoArgsConstructor
public class PnServiceDeskOperations {

    public static final String COL_OPERATION_ID = "operationId";
    public static final String COL_TICKET_ID = "ticketId";
    public static final String COL_STATUS = "status";
    public static final String COL_OPERATION_START_DATE = "operationStartDate";
    public static final String COL_OPERATION_LAST_UPDATE_DATE = "operationLastUpdateDate";
    public static final String COL_RECIPIENT_INTERNAL_ID = "recipientInternalId";
    public static final String RECIPIENT_INTERNAL_INDEX = "recipientInternalId-index";
    public static final String COL_ERROR_REASON = "errorReason";
    public static final String COL_EVENTS = "events";
    public static final String COL_ATTACHMENTS = "attachments";
    public static final String COL_TICKET_DATE = "ticketDate";
    public static final String COL_VR_DATE = "vrDate";
    public static final String COL_IUN = "iun";


    @Getter(onMethod = @__({@DynamoDbPartitionKey,@DynamoDbAttribute(COL_OPERATION_ID)}))
    private String operationId;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_TICKET_ID)}))
    private String ticketId;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_STATUS)}))
    private String status;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_OPERATION_START_DATE)}))
    private Instant operationStartDate;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_OPERATION_LAST_UPDATE_DATE)}))
    private Instant operationLastUpdateDate;

    @Getter(onMethod = @__({@DynamoDbSecondaryPartitionKey(indexNames = RECIPIENT_INTERNAL_INDEX),@DynamoDbAttribute(COL_RECIPIENT_INTERNAL_ID)}))
    private String recipientInternalId;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_ERROR_REASON)}))
    private String errorReason;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_EVENTS)}))
    private List<PnServiceDeskEvents> events;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_ATTACHMENTS)}))
    private List<PnServiceDeskAttachments> attachments;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_TICKET_DATE)}))
    private String ticketDate;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_VR_DATE)}))
    private String vrDate;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_IUN)}))
    private String iun;


}
