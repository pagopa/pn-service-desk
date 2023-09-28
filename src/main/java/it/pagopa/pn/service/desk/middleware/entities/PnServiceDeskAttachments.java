package it.pagopa.pn.service.desk.middleware.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.List;

@DynamoDbBean
@Getter
@Setter
@ToString
public class PnServiceDeskAttachments {

    public static final String COL_IUN = "iun";
    public static final String COL_FILES_KEY = "filesKey";
    public static final String COL_IS_AVAILABLE = "isAvailable";
    public static final String COL_IS_NOTIFIED = "isNotified";
    public static final String COL_PAGE_NUMBER = "numberOfPages";

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_IUN)}))
    private String iun;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_FILES_KEY)}))
    private List<String> filesKey;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_IS_AVAILABLE)}))
    private Boolean isAvailable;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_IS_NOTIFIED)}))
    private Boolean isNotified;

    @Getter(onMethod = @__({@DynamoDbAttribute(COL_PAGE_NUMBER)}))
    private Integer numberOfPages;

}
