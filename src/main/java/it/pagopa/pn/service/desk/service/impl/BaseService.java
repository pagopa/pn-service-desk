package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.mapper.OperationDtoMapper;
import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.model.OperationDto;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CustomLog
@AllArgsConstructor
public class BaseService {

    private final OperationDAO operationDAO;

    protected Mono<Long> checkNotificationFailedCount(String taxId, List<String> iuns) {
        return this.operationDAO.searchOperationsFromRecipientInternalId(taxId)
                .collectList()
                .flatMap(operation -> {
                    if (operation.isEmpty()) return Mono.just(1L);
                    else {
                        return operationContainsIuns(operation, iuns)
                                .doOnNext(iunsToSend -> log.info("unreachable iun {}", iunsToSend))
                                .collectList()
                                .flatMap(lst -> {
                                    if (lst.isEmpty()) {
                                        iuns.stream().forEach(i -> log.info("unreachable iun {}", i));
                                        return Mono.just(1L);
                                    }
                                    else return Mono.just(lst.size()  > 0 ? 1L : 0L);
                                });
                    }
                });
    }

    protected Flux<String> operationContainsIuns(List<PnServiceDeskOperations> operations, List<String> iuns){
        return Flux.fromStream(operations.stream())
                .flatMap(operation -> retrieveOperationWithIuns(operation, iuns))
                .collectList()
                .flatMapMany(lst -> {
                    Set<String> unreachableIuns = lst.stream()
                            .filter(l -> StringUtils.equals(l.getStatus(), OperationStatusEnum.KO.toString()))
                            .map(i -> i.getIun())
                            .collect(Collectors.toSet());

                    unreachableIuns.removeAll(lst.stream()
                            .filter(l -> !StringUtils.equals(l.getStatus(), OperationStatusEnum.KO.toString()))
                            .map(i -> i.getIun())
                            .distinct()
                            .collect(Collectors.toList()));
                    return Flux.fromIterable(unreachableIuns);
                });
    }

    private Flux<OperationDto> retrieveOperationWithIuns(PnServiceDeskOperations operation, List<String> iuns) {
        return Flux.fromStream(operation.getAttachments().stream())
                .filter(a -> a.getIsAvailable() == Boolean.TRUE && iuns.contains(a.getIun()))
                .map(attachments -> {
                    log.info("common iun {} with operationId:{} status:{}", attachments.getIun(), operation.getOperationId(), operation.getStatus());
                    return OperationDtoMapper.initOperation(operation, attachments.getIun());
                });
    }

}
