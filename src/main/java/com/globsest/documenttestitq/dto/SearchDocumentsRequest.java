package com.globsest.documenttestitq.dto;

import com.globsest.documenttestitq.entity.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchDocumentsRequest {

    private DocumentStatus status;
    private String author;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private int page = 0;
    private int size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}
