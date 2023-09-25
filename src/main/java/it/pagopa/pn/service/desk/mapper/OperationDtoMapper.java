package it.pagopa.pn.service.desk.mapper;

import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.model.OperationDto;

public class OperationDtoMapper {

    private OperationDtoMapper(){}

    public static OperationDto initOperation(PnServiceDeskOperations operation, String iun){
        OperationDto operationDto = new OperationDto();
        operationDto.setOperationId(operation.getOperationId());
        operationDto.setStatus(operation.getStatus());
        operationDto.setIun(iun);
        return operationDto;
    }

}
