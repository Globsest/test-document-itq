package com.globsest.documenttestitq.service;

import com.globsest.documenttestitq.entity.Document;
import com.globsest.documenttestitq.entity.DocumentStatus;
import com.globsest.documenttestitq.repository.DocumentRepository;
import com.globsest.documenttestitq.util.DocumentNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentNumberGenerator numberGenerator;

    @Mock
    private DocumentHistoryService historyService;

    @InjectMocks
    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        when(numberGenerator.generateUniqueNumber()).thenReturn("DOC-20240101-12345");
    }

    @Test
    void testCreateDocument_Success() {
        // Given
        String author = "Иванов И.И.";
        String title = "Тестовый документ";
        String initiator = "admin";

        Document savedDocument = new Document();
        savedDocument.setId(1L);
        savedDocument.setNumber("DOC-20240101-12345");
        savedDocument.setAuthor(author);
        savedDocument.setTitle(title);
        savedDocument.setStatus(DocumentStatus.DRAFT);

        when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);

        // When
        Document result = documentService.createDocument(author, title, initiator);

        // Then
        assertNotNull(result);
        assertEquals(author, result.getAuthor());
        assertEquals(title, result.getTitle());
        assertEquals(DocumentStatus.DRAFT, result.getStatus());
        assertEquals("DOC-20240101-12345", result.getNumber());
        verify(documentRepository, times(1)).save(any(Document.class));
        verify(numberGenerator, times(1)).generateUniqueNumber();
    }
}
