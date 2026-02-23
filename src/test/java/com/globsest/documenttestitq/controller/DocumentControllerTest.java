package com.globsest.documenttestitq.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.globsest.documenttestitq.dto.CreateDocumentRequest;
import com.globsest.documenttestitq.entity.Document;
import com.globsest.documenttestitq.entity.DocumentStatus;
import com.globsest.documenttestitq.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DocumentService documentService;

    @Test
    void testCreateDocument_Success() throws Exception {
        // Given
        CreateDocumentRequest request = new CreateDocumentRequest();
        request.setAuthor("Иванов И.И.");
        request.setTitle("Тестовый документ");
        request.setInitiator("admin");

        Document document = new Document();
        document.setId(1L);
        document.setNumber("DOC-20240101-12345");
        document.setAuthor("Иванов И.И.");
        document.setTitle("Тестовый документ");
        document.setStatus(DocumentStatus.DRAFT);
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());

        when(documentService.createDocument(any(), any(), any())).thenReturn(document);

        // When & Then
        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.number").value("DOC-20240101-12345"))
                .andExpect(jsonPath("$.author").value("Иванов И.И."))
                .andExpect(jsonPath("$.title").value("Тестовый документ"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void testGetDocument_Success() throws Exception {
        // Given
        Long documentId = 1L;
        Document document = new Document();
        document.setId(documentId);
        document.setNumber("DOC-20240101-12345");
        document.setAuthor("Иванов И.И.");
        document.setTitle("Тестовый документ");
        document.setStatus(DocumentStatus.DRAFT);
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());

        when(documentService.getDocumentById(documentId)).thenReturn(document);
        when(documentService.getDocumentHistory(documentId)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/documents/{id}", documentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(documentId))
                .andExpect(jsonPath("$.number").value("DOC-20240101-12345"));
    }
}
