package com.globsest.documenttestitq.controller;

import com.globsest.documenttestitq.dto.*;
import com.globsest.documenttestitq.entity.Document;
import com.globsest.documenttestitq.entity.DocumentStatus;
import com.globsest.documenttestitq.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(@Valid @RequestBody CreateDocumentRequest request) {
        log.info("Создание документа: автор={}, название={}, инициатор={}", 
                request.getAuthor(), request.getTitle(), request.getInitiator());
        
        Document document = documentService.createDocument(
                request.getAuthor(),
                request.getTitle(),
                request.getInitiator()
        );

        DocumentResponse response = DocumentResponse.fromEntity(document);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable Long id) {
        Document document = documentService.getDocumentById(id);
        List<DocumentResponse.HistoryEntryResponse> history = documentService.getDocumentHistory(id)
                .stream()
                .map(DocumentResponse.HistoryEntryResponse::fromEntity)
                .collect(Collectors.toList());

        DocumentResponse response = DocumentResponse.fromEntity(document);
        response.setHistory(history);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getDocuments(
            @RequestParam(required = false) List<Long> ids) {
        
        if (ids != null && !ids.isEmpty()) {
            List<Document> documents = documentService.getDocumentsByIds(ids);
            List<DocumentResponse> responses = documents.stream()
                    .map(DocumentResponse::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } else {
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Page<DocumentResponse>> searchDocuments(
            @RequestParam(required = false) DocumentStatus status,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) LocalDateTime dateFrom,
            @RequestParam(required = false) LocalDateTime dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("ASC") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Document> documents = documentService.findDocuments(
                status, author, dateFrom, dateTo, pageable);

        Page<DocumentResponse> responses = documents.map(DocumentResponse::fromEntity);
        return ResponseEntity.ok(responses);
    }
}
