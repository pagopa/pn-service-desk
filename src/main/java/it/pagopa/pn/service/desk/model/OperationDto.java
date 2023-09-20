package it.pagopa.pn.service.desk.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@Setter
@ToString
public class OperationDto {
    private String operationId;
    private String status;
    private String iun;

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof OperationDto)) {
            return false;
        }

        OperationDto c = (OperationDto) o;
        return operationId.equals(c.getIun());
    }

}
