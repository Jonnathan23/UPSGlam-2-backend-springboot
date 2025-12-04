package com.main.spring.app.model;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class ErrorResponse {
    private String message;
    private String error;
    private int status;
    private Map<String, String> details;
}
