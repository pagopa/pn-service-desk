package it.pagopa.pn.service.desk.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

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
        if (iun.equals(c.getIun())) {
            // equals if same status
            if (status.equals(c.getStatus())) {
                if (StringUtils.equals(status, OperationStatusEnum.KO.toString())) return false;
                else return true;
            }
            else {
                // equals if status KO and other one is !KO
                return (StringUtils.equals(status, OperationStatusEnum.KO.toString()) && !StringUtils.equals(c.getStatus(), OperationStatusEnum.KO.toString())
                || StringUtils.equals(c.getStatus(), OperationStatusEnum.KO.toString()) && !StringUtils.equals(status, OperationStatusEnum.KO.toString()));
            }
        }

        return iun.equals(c.getIun());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.iun);
    }

}
