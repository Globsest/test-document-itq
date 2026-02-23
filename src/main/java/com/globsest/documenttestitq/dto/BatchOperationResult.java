package com.globsest.documenttestitq.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchOperationResult {

    private Long id;
    private boolean success;
    private String errorCode;
    private String errorMessage;
}
