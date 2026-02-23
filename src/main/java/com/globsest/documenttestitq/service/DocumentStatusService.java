package com.globsest.documenttestitq.service;

import com.globsest.documenttestitq.dto.BatchOperationResult;
import com.globsest.documenttestitq.dto.BatchOperationResponse;
import com.globsest.documenttestitq.exception.ApprovalRegistryException;
import com.globsest.documenttestitq.exception.DocumentNotFoundException;
import com.globsest.documenttestitq.exception.InvalidStatusTransitionException;
import com.globsest.documenttestitq.entity.Document;
import com.globsest.documenttestitq.entity.DocumentAction;
import com.globsest.documenttestitq.entity.DocumentStatus;
import com.globsest.documenttestitq.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.StaleObjectStateException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentStatusService {

    private final DocumentRepository documentRepository;
    private final DocumentHistoryService historyService;
    private final ApprovalRegistryService registryService;

    @Transactional
    public BatchOperationResponse submitDocuments(List<Long> ids, String initiator, String comment) {
        log.info("Начало пакетной отправки на согласование: {} документов, инициатор: {}", ids.size(), initiator);

        List<BatchOperationResult> results = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        for (Long id : ids) {
            BatchOperationResult result = submitDocument(id, initiator, comment);
            results.add(result);
            if (result.isSuccess()) {
                successCount++;
            } else {
                failureCount++;
            }
        }

        log.info("Завершена пакетная отправка на согласование: успешно {}, ошибок {}", successCount, failureCount);

        return BatchOperationResponse.builder()
                .results(results)
                .totalCount(ids.size())
                .successCount(successCount)
                .failureCount(failureCount)
                .build();
    }

    @Transactional(noRollbackFor = {DocumentNotFoundException.class, InvalidStatusTransitionException.class})
    public BatchOperationResult submitDocument(Long id, String initiator, String comment) {
        try {
            Document document = documentRepository.findById(id)
                    .orElseThrow(() -> new DocumentNotFoundException(id));

            if (document.getStatus() != DocumentStatus.DRAFT) {
                return BatchOperationResult.builder()
                        .id(id)
                        .success(false)
                        .errorCode("INVALID_STATUS_TRANSITION")
                        .errorMessage("Документ должен быть в статусе DRAFT, текущий статус: " + document.getStatus())
                        .build();
            }

            document.setStatus(DocumentStatus.SUBMITTED);
            documentRepository.save(document);
            historyService.addHistoryEntry(document, DocumentAction.SUBMIT, initiator, comment);

            log.debug("Документ {} отправлен на согласование пользователем {}", id, initiator);

            return BatchOperationResult.builder()
                    .id(id)
                    .success(true)
                    .build();

        } catch (DocumentNotFoundException e) {
            return BatchOperationResult.builder()
                    .id(id)
                    .success(false)
                    .errorCode("NOT_FOUND")
                    .errorMessage(e.getMessage())
                    .build();
        } catch (StaleObjectStateException e) {
            return BatchOperationResult.builder()
                    .id(id)
                    .success(false)
                    .errorCode("CONCURRENT_MODIFICATION")
                    .errorMessage("Документ был изменен другим пользователем")
                    .build();
        } catch (Exception e) {
            log.error("Ошибка при отправке документа {} на согласование: {}", id, e.getMessage(), e);
            return BatchOperationResult.builder()
                    .id(id)
                    .success(false)
                    .errorCode("INTERNAL_ERROR")
                    .errorMessage("Внутренняя ошибка: " + e.getMessage())
                    .build();
        }
    }

    @Transactional
    public BatchOperationResponse approveDocuments(List<Long> ids, String initiator, String comment) {
        log.info("Начало пакетного утверждения: {} документов, инициатор: {}", ids.size(), initiator);

        List<BatchOperationResult> results = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        for (Long id : ids) {
            BatchOperationResult result = approveDocument(id, initiator, comment);
            results.add(result);
            if (result.isSuccess()) {
                successCount++;
            } else {
                failureCount++;
            }
        }

        log.info("Завершено пакетное утверждение: успешно {}, ошибок {}", successCount, failureCount);

        return BatchOperationResponse.builder()
                .results(results)
                .totalCount(ids.size())
                .successCount(successCount)
                .failureCount(failureCount)
                .build();
    }

    @Transactional(rollbackFor = ApprovalRegistryException.class)
    public BatchOperationResult approveDocument(Long id, String initiator, String comment) {
        try {
            Document document = documentRepository.findById(id)
                    .orElseThrow(() -> new DocumentNotFoundException(id));

            if (document.getStatus() != DocumentStatus.SUBMITTED) {
                return BatchOperationResult.builder()
                        .id(id)
                        .success(false)
                        .errorCode("INVALID_STATUS_TRANSITION")
                        .errorMessage("Документ должен быть в статусе SUBMITTED, текущий статус: " + document.getStatus())
                        .build();
            }

            document.setStatus(DocumentStatus.APPROVED);
            documentRepository.save(document);

            historyService.addHistoryEntry(document, DocumentAction.APPROVE, initiator, comment);

            try {
                registryService.createRegistryEntry(id, initiator);
            } catch (Exception e) {
                log.error("Ошибка создания записи в реестре для документа {}: {}", id, e.getMessage());
                throw new ApprovalRegistryException("Не удалось создать запись в реестре утверждений: " + e.getMessage(), e);
            }

            log.debug("Документ {} утвержден пользователем {}", id, initiator);

            return BatchOperationResult.builder()
                    .id(id)
                    .success(true)
                    .build();

        } catch (DocumentNotFoundException e) {
            return BatchOperationResult.builder()
                    .id(id)
                    .success(false)
                    .errorCode("NOT_FOUND")
                    .errorMessage(e.getMessage())
                    .build();
        } catch (ApprovalRegistryException e) {
            log.error("Ошибка реестра утверждений для документа {}: {}", id, e.getMessage());
            return BatchOperationResult.builder()
                    .id(id)
                    .success(false)
                    .errorCode("APPROVAL_REGISTRY_ERROR")
                    .errorMessage(e.getMessage())
                    .build();
        } catch (StaleObjectStateException e) {
            return BatchOperationResult.builder()
                    .id(id)
                    .success(false)
                    .errorCode("CONCURRENT_MODIFICATION")
                    .errorMessage("Документ был изменен другим пользователем")
                    .build();
        } catch (Exception e) {
            log.error("Ошибка при утверждении документа {}: {}", id, e.getMessage(), e);
            return BatchOperationResult.builder()
                    .id(id)
                    .success(false)
                    .errorCode("INTERNAL_ERROR")
                    .errorMessage("Внутренняя ошибка: " + e.getMessage())
                    .build();
        }
    }
}
