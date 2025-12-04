package com.main.spring.app.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.main.spring.app.interfaces.auth.AuthService;
import com.main.spring.app.model.RegisterRequest;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;



@RestController
@RequestMapping("/api/auth")
public class AuthControllerImpl {

    private final AuthService authService;

    public AuthControllerImpl(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    // 👈 ¡IMPORTANTE! Añadimos @Valid para activar las validaciones del DTO
    public Mono<String> registerUser(@Valid @RequestBody RegisterRequest request) {

        System.out.println("Petición de registro recibida para: " + request.getEmail());

        return authService.registerUser(request)
                .onErrorResume(e -> {
                    // Captura el error de Firebase (ej: Email ya existe)
                    String message = e.getMessage();
                    if (message != null && message.contains("EMAIL_ALREADY_EXISTS")) {
                        return Mono.error(
                                new ResponseStatusException(HttpStatus.CONFLICT, "Email ya registrado en Firebase."));
                    }
                    // Si es un error de validación, Spring lo maneja automáticamente (400 Bad
                    // Request)
                    return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error interno del servidor."));
                });
    }
}
