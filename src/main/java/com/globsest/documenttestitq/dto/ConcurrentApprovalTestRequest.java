package com.globsest.documenttestitq.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConcurrentApprovalTestRequest {

    @NotNull(message = "ID документа обязателен")
    private Long documentId;

    @Min(value = 1, message = "Количество потоков должно быть не менее 1")
    private int threads = 5;

    @Min(value = 1, message = "Количество попыток должно быть не менее 1")
    private int attempts = 10;

    @NotNull(message = "Инициатор обязателен")
    private String initiator;
}
