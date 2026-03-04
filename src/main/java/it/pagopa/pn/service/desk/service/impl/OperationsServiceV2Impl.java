package it.pagopa.pn.service.desk.service.impl;


import it.pagopa.pn.service.desk.exception.PnGenericException;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.GetOperationsResponseV2;
import it.pagopa.pn.service.desk.generated.openapi.server.v1.dto.OperationDetail;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.service.OperationsServiceV2;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.service.desk.exception.ExceptionTypeEnum.*;

@Slf4j
@Service
public class OperationsServiceV2Impl implements OperationsServiceV2 {


    private final OperationDAO operationDAO;

    public OperationsServiceV2Impl(OperationDAO operationDAO) {
        this.operationDAO = operationDAO;
    }


    @Override
    public Mono<GetOperationsResponseV2> getOperation(String operationId) {
        GetOperationsResponseV2 response = new GetOperationsResponseV2();
        log.info("Getting operation with id {}", operationId);
        return operationDAO.getByOperationId(operationId)
                           .switchIfEmpty(Mono.defer(() -> {
                               log.warn("Operation with id {} not found", operationId);
                               return Mono.error(new PnGenericException(OPERATION_IS_NOT_PRESENT, OPERATION_IS_NOT_PRESENT.getMessage(), HttpStatus.NOT_FOUND));
                           }))
                           .doOnNext(op -> log.info("Operation with id {} found, status: {}", operationId, op.getStatus()))
                           .filter(operation -> !Boolean.TRUE.equals(operation.getIsSubOperation()))
                           .switchIfEmpty(Mono.defer(() -> {
                               log.warn("Operation is a sub-operation - operationId={}", operationId);
                               return Mono.error(new PnGenericException(OPERATION_IS_NOT_PRESENT, OPERATION_IS_NOT_PRESENT.getMessage(), HttpStatus.NOT_FOUND));
                           }))
                           .flatMap(operation -> {
                               response.setStatus(operation.getStatus());
                               response.setErrorReason(operation.getErrorReason());
                               return StringUtils.isNotBlank(operation.getIun())
                                       ? buildOperationResponseV1(operation, response).doOnSuccess( res -> log.info("getOperation completed for V1 operation - operationId={}, finalStatus={}", operationId, res.getStatus()))
                                       : Flux.fromIterable(operation.getSubOperationsIds())
                                          .flatMap(operationDAO::getByOperationId)
                                             .doOnNext(subOp -> log.info("SubOperation - id={}, status={}", subOp.getOperationId(), subOp.getStatus()))
                                             .map(this::toOperationDetail)
                                          .doOnNext(response::addSubOperationsItem)
                                          .then(Mono.just(response))
                                             .doOnSuccess(res ->
                                                                  log.info("getOperation completed - operationId={}, finalStatus={}, subOperationsCount={}", operationId, res.getStatus(), res.getSubOperations() != null ? res.getSubOperations().size() : 0));})
                           .onErrorResume(WebClientResponseException.class, exception -> {
                               log.error( "Error during get operation with id {}, error: {}", operationId, exception.getMessage());
                               return Mono.error(new PnGenericException(ERROR_DURING_GET_OPERATION_V2, ERROR_DURING_GET_OPERATION_V2.getMessage(), HttpStatus.BAD_REQUEST));                           });
    }

    private static Mono<GetOperationsResponseV2> buildOperationResponseV1(PnServiceDeskOperations operation, GetOperationsResponseV2 response) {
         log.info("Operation with id {} is a V1-operation, building response with iun {}", operation.getOperationId(), operation.getIun());
            response.setIun(operation.getIun());

            return Mono.just(response);
        }



    private OperationDetail toOperationDetail(PnServiceDeskOperations subOperation) {
        OperationDetail detail = new OperationDetail();
        detail.setStatus(subOperation.getStatus());
        detail.setIun(subOperation.getIun());
        detail.setErrorReason(subOperation.getErrorReason());
        return detail;
    }





}


