package com.globsest.documenttestitq.dto;

import com.globsest.documenttestitq.entity.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConcurrentApprovalTestResponse {

    private Long documentId;
    private int totalAttempts;
    private int successCount;
    private int conflictCount;
    private int errorCount;
    private DocumentStatus finalStatus;
    private int registryEntriesCount;
}
