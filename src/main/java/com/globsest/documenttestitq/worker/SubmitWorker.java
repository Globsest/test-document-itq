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
public class SubmitWorker {

    private final DocumentRepository documentRepository;
    private final DocumentStatusService statusService;

    @Value("${workers.submit.enabled:true}")
    private boolean enabled;

    @Value("${workers.submit.batchSize:100}")
    private int batchSize;

    private static final String INITIATOR = "submit-worker";

    @Scheduled(fixedDelayString = "${workers.submit.interval:5000}")
    public void processDraftDocuments() {
        if (!enabled) {
            return;
        }
        List<Document> batch = documentRepository.findByStatusOrderByIdAsc(
                DocumentStatus.DRAFT,
                PageRequest.of(0, batchSize)
        );
        if (batch.isEmpty()) {
            return;
        }
        long totalDraft = documentRepository.countByStatus(DocumentStatus.DRAFT);
        log.info("SUBMIT-worker: найдено DRAFT: {}, обрабатываю пачку {}", totalDraft, batch.size());
        List<Long> ids = batch.stream().map(Document::getId).collect(Collectors.toList());
        long start = System.currentTimeMillis();
        try {
            BatchOperationResponse response = statusService.submitDocuments(ids, INITIATOR, "Фоновая отправка");
            long elapsed = System.currentTimeMillis() - start;
            long remaining = documentRepository.countByStatus(DocumentStatus.DRAFT);
            log.info("SUBMIT-worker: отправлена пачка {} документов за {} мс, успешно {}, ошибок {}, осталось DRAFT: {}",
                    ids.size(), elapsed, response.getSuccessCount(), response.getFailureCount(), remaining);
        } catch (Exception e) {
            log.error("SUBMIT-worker: ошибка при отправке пачки {} документов: {}", ids.size(), e.getMessage(), e);
        }
    }
}
