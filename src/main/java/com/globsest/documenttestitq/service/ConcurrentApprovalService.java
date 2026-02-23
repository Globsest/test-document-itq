package com.globsest.documenttestitq.service;

import com.globsest.documenttestitq.dto.BatchOperationResult;
import com.globsest.documenttestitq.dto.ConcurrentApprovalTestResponse;
import com.globsest.documenttestitq.entity.Document;
import com.globsest.documenttestitq.entity.DocumentStatus;
import com.globsest.documenttestitq.repository.ApprovalRegistryRepository;
import com.globsest.documenttestitq.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConcurrentApprovalService {

    private final DocumentStatusService statusService;
    private final DocumentRepository documentRepository;
    private final ApprovalRegistryRepository registryRepository;

    @Transactional(readOnly = true)
    public ConcurrentApprovalTestResponse runConcurrentApprovalTest(
            Long documentId, int threads, int attempts, String initiator) {

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        int totalTasks = threads * attempts;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(totalTasks);

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < totalTasks; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    errorCount.incrementAndGet();
                    doneLatch.countDown();
                    return;
                }
                BatchOperationResult result = statusService.approveDocument(documentId, initiator, null);
                if (result.isSuccess()) {
                    successCount.incrementAndGet();
                } else if ("INVALID_STATUS_TRANSITION".equals(result.getErrorCode())
                        || "CONCURRENT_MODIFICATION".equals(result.getErrorCode())) {
                    conflictCount.incrementAndGet();
                } else {
                    errorCount.incrementAndGet();
                }
                doneLatch.countDown();
            });
        }

        startLatch.countDown();
        try {
            doneLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        executor.shutdown();

        Document document = documentRepository.findById(documentId).orElse(null);
        DocumentStatus finalStatus = document != null ? document.getStatus() : null;
        int registryEntriesCount = registryRepository.existsByDocumentId(documentId) ? 1 : 0;

        return ConcurrentApprovalTestResponse.builder()
                .documentId(documentId)
                .totalAttempts(totalTasks)
                .successCount(successCount.get())
                .conflictCount(conflictCount.get())
                .errorCount(errorCount.get())
                .finalStatus(finalStatus)
                .registryEntriesCount(registryEntriesCount)
                .build();
    }
}
