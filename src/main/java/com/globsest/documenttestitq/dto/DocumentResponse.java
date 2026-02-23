package com.globsest.documenttestitq.dto;

import com.globsest.documenttestitq.entity.Document;
import com.globsest.documenttestitq.entity.DocumentHistory;
import com.globsest.documenttestitq.entity.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponse {

    private Long id;
    private String number;
    private String author;
    private String title;
    private DocumentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<HistoryEntryResponse> history;

    public static DocumentResponse fromEntity(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .number(document.getNumber())
                .author(document.getAuthor())
                .title(document.getTitle())
                .status(document.getStatus())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .history(null)
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HistoryEntryResponse {
        private Long id;
        private String action;
        private String performedBy;
        private LocalDateTime performedAt;
        private String comment;

        public static HistoryEntryResponse fromEntity(DocumentHistory history) {
            return HistoryEntryResponse.builder()
                    .id(history.getId())
                    .action(history.getAction().name())
                    .performedBy(history.getPerformedBy())
                    .performedAt(history.getPerformedAt())
                    .comment(history.getComment())
                    .build();
        }
    }
}
