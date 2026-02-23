package com.globsest.documenttestitq.controller;

import com.globsest.documenttestitq.dto.*;
import com.globsest.documenttestitq.service.DocumentStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentStatusController {

    private final DocumentStatusService statusService;

    @PostMapping("/submit")
    public ResponseEntity<BatchOperationResponse> submitDocuments(
            @Valid @RequestBody BatchSubmitRequest request) {
        
        log.info("Запрос на отправку на согласование: {} документов", request.getIds().size());
        
        BatchOperationResponse response = statusService.submitDocuments(
                request.getIds(),
                request.getInitiator(),
                request.getComment()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/approve")
    public ResponseEntity<BatchOperationResponse> approveDocuments(
            @Valid @RequestBody BatchApproveRequest request) {
        
        log.info("Запрос на утверждение: {} документов", request.getIds().size());
        
        BatchOperationResponse response = statusService.approveDocuments(
                request.getIds(),
                request.getInitiator(),
                request.getComment()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/test-concurrent-approval")
    public ResponseEntity<ConcurrentApprovalTestResponse> testConcurrentApproval(
            @Valid @RequestBody ConcurrentApprovalTestRequest request) {
        
        log.info("Тест конкурентного утверждения: documentId={}, threads={}, attempts={}", 
                request.getDocumentId(), request.getThreads(), request.getAttempts());
        
        // TODO: Реализовать в День 2
        // Пока возвращаем заглушку
        ConcurrentApprovalTestResponse response = ConcurrentApprovalTestResponse.builder()
                .documentId(request.getDocumentId())
                .totalAttempts(0)
                .successCount(0)
                .conflictCount(0)
                .errorCount(0)
                .build();

        return ResponseEntity.ok(response);
    }
}
