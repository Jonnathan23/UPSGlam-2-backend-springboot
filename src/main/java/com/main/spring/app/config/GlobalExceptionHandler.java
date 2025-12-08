package com.main.spring.app.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;

import com.main.spring.app.model.ErrorResponse;

import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private String defaultErrorMessage = "Ocurrió un error inesperado";

    @ExceptionHandler(org.springframework.web.reactive.function.client.WebClientRequestException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleWebClientRequestException(
            org.springframework.web.reactive.function.client.WebClientRequestException ex) {
        if (isConnectionRefused(ex)) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .message("Servidor FastAPI No prendido o conectado")
                    .error("Service Unavailable")
                    .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                    .details(null)
                    .build();
            return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE));
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Error de comunicación con el servicio de procesamiento de imágenes.")
                .error("Internal Server Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .details(null)
                .build();
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private boolean isConnectionRefused(Throwable ex) {
        if (ex == null)
            return false;
        if (ex instanceof java.net.ConnectException)
            return true;
        if (ex.getMessage() != null && ex.getMessage().contains("Connection refused"))
            return true;
        return isConnectionRefused(ex.getCause());
    }

    // 1. Maneja ResponseStatusException (errores específicos del controller)
    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleResponseStatusException(ResponseStatusException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(ex.getStatusCode().value())
                .error(ex.getStatusCode().toString())
                .message(ex.getReason() != null ? ex.getReason() : "Error en la petición")
                .build();

        return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(errorResponse));
    }

    // 2. Método para interceptar errores de validación (@Valid)
    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
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

    // 3. Maneja errores genéricos de servidor - Estado 500
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericErrors(Exception ex) {

        // Aquí puedes registrar el error
        ex.printStackTrace();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("Ocurrió un error inesperado en el servidor.")
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
    }

}
