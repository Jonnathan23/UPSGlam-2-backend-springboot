package com.main.spring.app.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.main.spring.app.model.ErrorResponse;

import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private String defaultErrorMessage = "Ocurrió un error inesperado";

    // 2. Método para interceptar errores de validación (@Valid)
    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    // 👈 Cambiamos el tipo de retorno a ErrorResponse
    public Mono<ResponseEntity<ErrorResponse>> handleValidationExceptions(WebExchangeBindException ex) {

        Map<String, String> details = new HashMap<>();

        // 1. Iterar y recopilar los errores de campo (como antes)
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            details.put(fieldName, errorMessage);
        });

        // 2. Construir la respuesta con el Builder
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message(defaultErrorMessage)
                .details(details) // Incluimos los detalles de campo
                .build();

        // Devolvemos el ResponseEntity con el objeto ErrorResponse
        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }

    // Maneja errores genéricos de servidor (Ejemplo) - Estado 500
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericErrors(Exception ex) {

        // Aquí puedes registrar el error 'ex.printStackTrace()'

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("Ocurrió un error inesperado en el servidor.")
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
    }

}
