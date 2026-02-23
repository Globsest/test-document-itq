package com.globsest.documenttestitq.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDocumentRequest {

    @NotBlank(message = "Автор не может быть пустым")
    private String author;

    @NotBlank(message = "Название не может быть пустым")
    private String title;

    @NotBlank(message = "Инициатор не может быть пустым")
    private String initiator;
}
