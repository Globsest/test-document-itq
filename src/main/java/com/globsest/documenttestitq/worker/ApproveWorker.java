package com.globsest.documenttestitq.worker;

import com.globsest.documenttestitq.dto.BatchOperationResponse;
import com.globsest.documenttestitq.entity.Document;
import com.globsest.documenttestitq.entity.DocumentStatus;
import com.globsest.documenttestitq.repository.DocumentRepository;
import com.globsest.documenttestitq.service.DocumentStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApproveWorker {

    private final DocumentRepository documentRepository;
    private final DocumentStatusService statusService;

    @Value("${workers.approve.enabled:true}")
    private boolean enabled;

    @Value("${workers.approve.batchSize:100}")
    private int batchSize;

    private static final String INITIATOR = "approve-worker";

    @Scheduled(fixedDelayString = "${workers.approve.interval:5000}")
    public void processSubmittedDocuments() {
        if (!enabled) {
            return;
        }
        List<Document> batch = documentRepository.findByStatusOrderByIdAsc(
                DocumentStatus.SUBMITTED,
                PageRequest.of(0, batchSize)
        );
        if (batch.isEmpty()) {
            return;
        }
        long totalSubmitted = documentRepository.countByStatus(DocumentStatus.SUBMITTED);
        log.info("APPROVE-worker: найдено SUBMITTED: {}, обрабатываю пачку {}", totalSubmitted, batch.size());
        List<Long> ids = batch.stream().map(Document::getId).collect(Collectors.toList());
        long start = System.currentTimeMillis();
        try {
            BatchOperationResponse response = statusService.approveDocuments(ids, INITIATOR, "Фоновое утверждение");
            long elapsed = System.currentTimeMillis() - start;
            long remaining = documentRepository.countByStatus(DocumentStatus.SUBMITTED);
            log.info("APPROVE-worker: утверждена пачка {} документов за {} мс, успешно {}, ошибок {}, осталось SUBMITTED: {}",
                    ids.size(), elapsed, response.getSuccessCount(), response.getFailureCount(), remaining);
        } catch (Exception e) {
            log.error("APPROVE-worker: ошибка при утверждении пачки {} документов: {}", ids.size(), e.getMessage(), e);
        }
    }
}
