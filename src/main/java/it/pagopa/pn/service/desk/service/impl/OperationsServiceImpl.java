package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.CreateOperationRequest;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.OperationsResponse;
import it.pagopa.pn.service.desk.mapper.AddressMapper;
import it.pagopa.pn.service.desk.mapper.OperationMapper;
import it.pagopa.pn.service.desk.middleware.db.dao.AddressDAO;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.middleware.msclient.DataVaultClient;
import it.pagopa.pn.service.desk.middleware.msclient.impl.DataVaultClientImpl;
import it.pagopa.pn.service.desk.service.OperationsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
public class OperationsServiceImpl implements OperationsService {
    @Autowired
    private DataVaultClient dataVaultClient;
    @Autowired
    private OperationDAO operationDAO;

    @Autowired
    private AddressDAO addressDAO;


    @Override
    public Mono<OperationsResponse> createOperation(String xPagopaPnUid, CreateOperationRequest createOperationRequest) {

        OperationsResponse response = new OperationsResponse();

        return dataVaultClient.anonymized(createOperationRequest.getTaxId())
                .zipWith(generateOperationId())
                .map(recipientIdAndOperationId -> {
                    return OperationMapper.getInitialOperation(recipientIdAndOperationId.getT2(), recipientIdAndOperationId.getT1(), createOperationRequest.getTicketId());
                })
                .zipWhen(pnServiceDeskOperations -> {
                    return Mono.just(AddressMapper.toEntity(createOperationRequest.getAddress(), pnServiceDeskOperations.getOperationId()));
                })
                .flatMap(operationAndAddress -> {
                    return operationDAO.createOperation(operationAndAddress.getT1())
                                    .doOnNext(operation -> addressDAO.createAddress(operationAndAddress.getT2()))
                                    .map(operation -> response.operationId(operationAndAddress.getT1().getOperationId()));
                });
    }

    private Mono<String> generateOperationId (){
        return Mono.just(UUID.randomUUID().toString());

    }


}
