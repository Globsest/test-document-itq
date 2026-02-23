package com.globsest.documenttestitq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchApproveRequest {

    @NotEmpty(message = "Список ID не может быть пустым")
    @Size(max = 1000, message = "Максимальное количество ID: 1000")
    private List<Long> ids;

    @NotBlank(message = "Инициатор не может быть пустым")
    private String initiator;

    private String comment;
}
