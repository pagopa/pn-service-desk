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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CustomLog
@AllArgsConstructor
public class BaseService {

    protected final OperationDAO operationDAO;

    protected Mono<Long> checkNotificationFailedCount(String taxId, List<String> iuns) {
        return checkNotificationFailed(taxId, iuns)
                .doOnNext(iunsToSend -> log.info("unreachable iun from operation {}", iunsToSend))
                .collectList()
                .flatMap(lst -> {
                    if (!lst.isEmpty()) {
                        lst.stream().forEach(i -> log.info("unreachable iun {}", i));
                    }
                    return Mono.just(!lst.isEmpty() ? 1L : 0L);
                });

    }

    protected Flux<String> checkNotificationFailedList(String taxId, List<String> iuns) {
        return checkNotificationFailed(taxId, iuns)
                .collectList()
                .flatMapMany(lst -> {
                    if (!lst.isEmpty()) {
                        lst.stream().forEach(i -> log.info("unreachable iun {}", i));
                    }
                    return Flux.fromIterable(lst);
                });
    }

    private Flux<String> checkNotificationFailed(String taxId, List<String> iuns) {
        return this.operationDAO.searchOperationsFromRecipientInternalId(taxId)
                .collectList()
                .flatMapMany(operation -> {
                    if (operation.isEmpty()) return Flux.fromIterable(iuns);
                    else {
                        return operationContainsIuns(operation, iuns);
                    }
                });
    }

    protected Flux<String> operationContainsIuns(List<PnServiceDeskOperations> operations, List<String> iuns){
        List<String> lstIuns = new ArrayList<>(iuns);
        return Flux.fromStream(operations.stream())
                .flatMap(operation -> retrieveOperationWithIuns(operation, lstIuns))
                .distinct()
                .collectList()
                .flatMapMany(lst -> {
                    if (lst.isEmpty()) {
                        lstIuns.stream().forEach(i -> log.info("unreachable iun from operation {}", i));
                        return Flux.fromIterable(lstIuns);
                    }

                    lstIuns.removeAll(lst.stream().map(OperationDto::getIun).toList());
                    Set<String> result = lst.stream()
                            .filter(i -> StringUtils.equals(i.getStatus(), OperationStatusEnum.KO.toString()))
                            .map(OperationDto::getIun)
                            .collect(Collectors.toSet());
                    result.addAll(lstIuns);
                    return Flux.fromStream(result.stream());
                });
    }

    private Flux<OperationDto> retrieveOperationWithIuns(PnServiceDeskOperations operation, List<String> iuns) {
        if (operation.getAttachments() != null && !operation.getAttachments().isEmpty()) {
            return Flux.fromStream(operation.getAttachments().stream())
                    .filter(a -> a.getIsAvailable() == Boolean.TRUE && iuns.contains(a.getIun()))
                    .map(attachments -> {
                        log.info("common iun {} with operationId:{} status:{}", attachments.getIun(), operation.getOperationId(), operation.getStatus());
                        return OperationDtoMapper.initOperation(operation, attachments.getIun());
                    });
        } else return Flux.empty();
    }

}
