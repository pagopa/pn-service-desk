package it.pagopa.pn.service.desk.service.impl;

import it.pagopa.pn.service.desk.middleware.db.dao.OperationDAO;
import it.pagopa.pn.service.desk.middleware.entities.PnServiceDeskOperations;
import it.pagopa.pn.service.desk.model.OperationStatusEnum;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                                .collectList()
                                .flatMap(iunsToSend -> {
                                    if (iunsToSend.isEmpty()) return Mono.just(0L);
                                    else return Mono.just(1L);
                                });
                    }
                });
    }

    protected Flux<String> operationContainsIuns(List<PnServiceDeskOperations> operations, List<String> iuns){
        Map<String, String> iunAndStatus = new HashMap<>();

        operations.parallelStream().forEach(operation -> {
            log.info("operationId: {} ", operation.getOperationId());

            if (operation.getAttachments() != null && !operation.getAttachments().isEmpty()) {
                List<String> pnIuns = operation.getAttachments().stream().map(a -> a.getIun()).collect(Collectors.toList());
                List<String> commonIuns = iuns.stream().filter(i -> pnIuns.contains(i)).collect(Collectors.toList());

                if (!commonIuns.isEmpty()) {
                    commonIuns.stream().forEach(i -> {
                        // set iun and its status. Status different from KO has priority
                        if (iunAndStatus.containsKey(i)) {
                            if (iunAndStatus.get(i).equals(OperationStatusEnum.KO.toString())) {
                                iunAndStatus.put(i, operation.getStatus());
                            }
                        } else {
                            iunAndStatus.put(i, operation.getStatus());
                        }
                    });
                } else {
                    iuns.stream().forEach(i -> iunAndStatus.put(i, OperationStatusEnum.KO.toString()));
                }
            }
        });

        return checkIunsToSend(iunAndStatus);
    }

    private Flux<String> checkIunsToSend(Map<String, String> iunAndStatus) {
        return Flux.fromStream(iunAndStatus.entrySet().stream()
                .filter(w -> w.getValue().equals(OperationStatusEnum.KO.toString()))
                .map(s -> {
                    log.info("iun to send {} ", s.getKey());
                    return s.getKey();
                }).collect(Collectors.toList()).stream());
    }

}
