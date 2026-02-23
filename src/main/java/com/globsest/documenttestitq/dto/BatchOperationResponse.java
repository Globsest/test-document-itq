package com.globsest.documenttestitq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchOperationResponse {

    private List<BatchOperationResult> results;
    private int totalCount;
    private int successCount;
    private int failureCount;
}
