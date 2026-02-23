package com.globsest.documenttestitq.controller;

import com.globsest.documenttestitq.dto.CreateDocumentRequest;
import com.globsest.documenttestitq.entity.Document;
import com.globsest.documenttestitq.entity.DocumentStatus;
import com.globsest.documenttestitq.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private DocumentController documentController;

    private final tools.jackson.databind.ObjectMapper objectMapper = new tools.jackson.databind.ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(documentController).build();
    }

    @Test
    void testCreateDocument_Success() throws Exception {
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

        mockMvc.perform(get("/api/documents/{id}", documentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(documentId))
                .andExpect(jsonPath("$.number").value("DOC-20240101-12345"));
    }
}
