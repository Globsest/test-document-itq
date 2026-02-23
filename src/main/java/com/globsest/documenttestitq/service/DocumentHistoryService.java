package com.globsest.documenttestitq.service;

import com.globsest.documenttestitq.entity.Document;
import com.globsest.documenttestitq.entity.DocumentAction;
import com.globsest.documenttestitq.entity.DocumentHistory;
import com.globsest.documenttestitq.repository.DocumentHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentHistoryService {

    private final DocumentHistoryRepository historyRepository;

    @Transactional
    public DocumentHistory addHistoryEntry(Document document, DocumentAction action, String initiator, String comment) {
        DocumentHistory history = new DocumentHistory();
        history.setDocument(document);
        history.setAction(action);
        history.setPerformedBy(initiator);
        history.setPerformedAt(LocalDateTime.now());
        history.setComment(comment);

        DocumentHistory saved = historyRepository.save(history);
        document.getHistory().add(saved);
        return saved;
    }

    public List<DocumentHistory> getHistoryByDocumentId(Long documentId) {
        return historyRepository.findByDocumentIdOrderByPerformedAtDesc(documentId);
    }
}
