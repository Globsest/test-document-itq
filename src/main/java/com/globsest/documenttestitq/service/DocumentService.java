package com.globsest.documenttestitq.service;

import com.globsest.documenttestitq.entity.Document;
import com.globsest.documenttestitq.entity.DocumentHistory;
import com.globsest.documenttestitq.entity.DocumentStatus;
import com.globsest.documenttestitq.repository.DocumentRepository;
import com.globsest.documenttestitq.util.DocumentNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentNumberGenerator numberGenerator;
    private final DocumentHistoryService historyService;

    @Transactional
    public Document createDocument(String author, String title, String initiator) {
        Document document = new Document();
        document.setAuthor(author);
        document.setTitle(title);
        document.setStatus(DocumentStatus.DRAFT);
        document.setNumber(numberGenerator.generateUniqueNumber());

        Document saved = documentRepository.save(document);
        log.info("Создан документ {} пользователем {}", saved.getNumber(), initiator);
        return saved;
    }

    @Transactional(readOnly = true)
    public Document getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new com.globsest.documenttestitq.exception.DocumentNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Document> getDocumentsByIds(List<Long> ids) {
        return documentRepository.findByIdIn(ids);
    }

    @Transactional(readOnly = true)
    public Page<Document> findDocuments(DocumentStatus status, String author, LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable) {
        if (status != null && author != null && dateFrom != null && dateTo != null) {
            return documentRepository.findByStatusAndAuthorAndCreatedAtBetween(status, author, dateFrom, dateTo, pageable);
        } else if (status != null && dateFrom != null && dateTo != null) {
            return documentRepository.findByStatusAndCreatedAtBetween(status, dateFrom, dateTo, pageable);
        } else if (status != null && author != null) {
            return documentRepository.findByStatusAndAuthorContainingIgnoreCase(status, author, pageable);
        } else if (status != null) {
            return documentRepository.findByStatus(status, pageable);
        } else if (author != null) {
            return documentRepository.findByAuthorContainingIgnoreCase(author, pageable);
        } else if (dateFrom != null && dateTo != null) {
            return documentRepository.findByCreatedAtBetween(dateFrom, dateTo, pageable);
        } else {
            return documentRepository.findAll(pageable);
        }
    }

    @Transactional(readOnly = true)
    public List<DocumentHistory> getDocumentHistory(Long documentId) {
        return historyService.getHistoryByDocumentId(documentId);
    }
}
