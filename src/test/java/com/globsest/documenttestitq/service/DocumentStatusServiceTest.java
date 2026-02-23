package com.globsest.documenttestitq.service;

import com.globsest.documenttestitq.dto.BatchOperationResponse;
import com.globsest.documenttestitq.exception.ApprovalRegistryException;
import com.globsest.documenttestitq.entity.Document;
import com.globsest.documenttestitq.entity.DocumentStatus;
import com.globsest.documenttestitq.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentStatusServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentHistoryService historyService;

    @Mock
    private ApprovalRegistryService registryService;

    @InjectMocks
    private DocumentStatusService statusService;

    private Document draftDocument;
    private Document submittedDocument;

    @BeforeEach
    void setUp() {
        draftDocument = new Document();
        draftDocument.setId(1L);
        draftDocument.setNumber("DOC-001");
        draftDocument.setStatus(DocumentStatus.DRAFT);

        submittedDocument = new Document();
        submittedDocument.setId(2L);
        submittedDocument.setNumber("DOC-002");
        submittedDocument.setStatus(DocumentStatus.SUBMITTED);
    }

    @Test
    void testSubmitDocuments_Success() {
        List<Long> ids = Arrays.asList(1L);
        when(documentRepository.findById(1L)).thenReturn(Optional.of(draftDocument));
        when(documentRepository.save(any(Document.class))).thenReturn(draftDocument);

        BatchOperationResponse response = statusService.submitDocuments(ids, "admin", "Комментарий");

        assertEquals(1, response.getTotalCount());
        assertEquals(1, response.getSuccessCount());
        assertEquals(0, response.getFailureCount());
        assertTrue(response.getResults().get(0).isSuccess());
        verify(documentRepository, times(1)).save(any(Document.class));
        verify(historyService, times(1)).addHistoryEntry(any(), any(), any(), any());
    }

    @Test
    void testSubmitDocuments_NotFound() {

        List<Long> ids = Arrays.asList(999L);
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());


        BatchOperationResponse response = statusService.submitDocuments(ids, "admin", null);


        assertEquals(1, response.getTotalCount());
        assertEquals(0, response.getSuccessCount());
        assertEquals(1, response.getFailureCount());
        assertFalse(response.getResults().get(0).isSuccess());
        assertEquals("NOT_FOUND", response.getResults().get(0).getErrorCode());
    }

    @Test
    void testApproveDocuments_Success() {

        List<Long> ids = Arrays.asList(2L);
        when(documentRepository.findById(2L)).thenReturn(Optional.of(submittedDocument));
        when(documentRepository.save(any(Document.class))).thenReturn(submittedDocument);
        when(registryService.createRegistryEntry(anyLong(), any())).thenReturn(null);


        BatchOperationResponse response = statusService.approveDocuments(ids, "admin", "Комментарий");


        assertEquals(1, response.getTotalCount());
        assertEquals(1, response.getSuccessCount());
        assertEquals(0, response.getFailureCount());
        assertTrue(response.getResults().get(0).isSuccess());
        verify(documentRepository, times(1)).save(any(Document.class));
        verify(historyService, times(1)).addHistoryEntry(any(), any(), any(), any());
        verify(registryService, times(1)).createRegistryEntry(anyLong(), any());
    }

    @Test
    void testApproveDocuments_RegistryError_Rollback() {

        List<Long> ids = Arrays.asList(2L);
        when(documentRepository.findById(2L)).thenReturn(Optional.of(submittedDocument));
        when(documentRepository.save(any(Document.class))).thenReturn(submittedDocument);
        when(registryService.createRegistryEntry(anyLong(), any()))
                .thenThrow(new ApprovalRegistryException("Ошибка создания записи"));


        BatchOperationResponse response = statusService.approveDocuments(ids, "admin", null);


        assertEquals(1, response.getTotalCount());
        assertEquals(0, response.getSuccessCount());
        assertEquals(1, response.getFailureCount());
        assertFalse(response.getResults().get(0).isSuccess());
        assertEquals("APPROVAL_REGISTRY_ERROR", response.getResults().get(0).getErrorCode());
        verify(registryService, times(1)).createRegistryEntry(anyLong(), any());
    }

    @Test
    void testApproveDocuments_PartialResults() {

        List<Long> ids = Arrays.asList(2L, 999L, 3L);
        
        Document submittedDoc2 = new Document();
        submittedDoc2.setId(3L);
        submittedDoc2.setStatus(DocumentStatus.SUBMITTED);

        when(documentRepository.findById(2L)).thenReturn(Optional.of(submittedDocument));
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());
        when(documentRepository.findById(3L)).thenReturn(Optional.of(submittedDoc2));
        
        when(documentRepository.save(any(Document.class))).thenReturn(submittedDocument);
        when(registryService.createRegistryEntry(anyLong(), any())).thenReturn(null);

        BatchOperationResponse response = statusService.approveDocuments(ids, "admin", null);

        assertEquals(3, response.getTotalCount());
        assertEquals(2, response.getSuccessCount());
        assertEquals(1, response.getFailureCount());
        assertTrue(response.getResults().get(0).isSuccess());
        assertFalse(response.getResults().get(1).isSuccess());
        assertTrue(response.getResults().get(2).isSuccess());
    }
}
