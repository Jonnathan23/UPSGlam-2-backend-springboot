package com.main.spring.app.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentRequest {
    @NotBlank(message = "El comentario no puede estar vacío")
    private String com_text;

}
