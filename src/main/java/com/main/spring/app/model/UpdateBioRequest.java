package com.main.spring.app.model;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateBioRequest {

    @Size(max = 500, message = "La biografía no puede exceder 500 caracteres")
    private String usr_bio;
}

