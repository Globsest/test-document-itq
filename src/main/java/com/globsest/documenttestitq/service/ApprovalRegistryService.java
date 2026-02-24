package com.globsest.documenttestitq.service;

import com.globsest.documenttestitq.entity.ApprovalRegistry;
import com.globsest.documenttestitq.exception.RegistryAlreadyExistsException;
import com.globsest.documenttestitq.repository.ApprovalRegistryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalRegistryService {

    private final ApprovalRegistryRepository registryRepository;

    @Transactional
    public ApprovalRegistry createRegistryEntry(Long documentId, String approvedBy) {
        if (registryRepository.existsByDocumentId(documentId)) {
            throw new IllegalStateException("Запись в реестре утверждений для документа " + documentId + " уже существует");
        }

        ApprovalRegistry registry = new ApprovalRegistry();
        registry.setDocumentId(documentId);
        registry.setApprovedAt(LocalDateTime.now());
        registry.setApprovedBy(approvedBy);

        try {
            ApprovalRegistry saved = registryRepository.save(registry);
            log.info("Создана запись в реестре утверждений для документа {}", documentId);
            return saved;
        } catch (DataIntegrityViolationException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("duplicate key") || msg.contains("unique constraint") || msg.contains("document_id")) {
                throw new RegistryAlreadyExistsException("Запись в реестре для документа " + documentId + " уже существует", e);
            }
            throw e;
        }
    }

    @Transactional
    public void deleteRegistryEntry(Long documentId) {
        registryRepository.findByDocumentId(documentId)
                .ifPresent(registry -> {
                    registryRepository.delete(registry);
                    log.info("Удалена запись из реестра утверждений для документа {}", documentId);
                });
    }

    public boolean existsByDocumentId(Long documentId) {
        return registryRepository.existsByDocumentId(documentId);
    }
}
