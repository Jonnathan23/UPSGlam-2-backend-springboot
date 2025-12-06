package com.main.spring.app.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.main.spring.app.interfaces.auth.AuthService;
import com.main.spring.app.model.auth.LoginRequest;
import com.main.spring.app.model.auth.RegisterRequest;

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
    public Mono<String> registerUser(@Valid @RequestBody RegisterRequest request) {

        // System.out.println("Petición de registro recibida para: " + request.getEmail());

        return authService.registerUser(request)
                .onErrorResume(e -> {
                    // Log para debugging
                    // System.out.println("Error en controller - Tipo: " + e.getClass().getName());
                    // System.out.println("Error en controller - Mensaje: " + e.getMessage());
                    
                    // Verificar el mensaje directo
                    String message = e.getMessage();
                    if (message != null && message.contains("EMAIL_ALREADY_EXISTS")) {
                        return Mono.error(
                                new ResponseStatusException(HttpStatus.CONFLICT, "Email ya registrado en Firebase."));
                    }
                    
                    // Verificar la causa (puede estar envuelta)
                    Throwable cause = e.getCause();
                    if (cause != null) {
                        String causeMessage = cause.getMessage();
                        // System.out.println("Causa del error - Tipo: " + cause.getClass().getName());
                        // System.out.println("Causa del error - Mensaje: " + causeMessage);
                        
                        if (causeMessage != null && causeMessage.contains("EMAIL_ALREADY_EXISTS")) {
                            return Mono.error(
                                    new ResponseStatusException(HttpStatus.CONFLICT, "Email ya registrado en Firebase."));
                        }
                        
                        // Verificar si es FirebaseAuthException
                        if (cause instanceof com.google.firebase.auth.FirebaseAuthException) {
                            com.google.firebase.auth.FirebaseAuthException firebaseEx = 
                                (com.google.firebase.auth.FirebaseAuthException) cause;
                            // getErrorCode() retorna un enum, lo convertimos a String
                            // Firebase usa ALREADY_EXISTS cuando el email ya existe
                            String errorCodeStr = firebaseEx.getErrorCode() != null ? 
                                firebaseEx.getErrorCode().name() : null;
                            if (errorCodeStr != null && (errorCodeStr.equals("ALREADY_EXISTS") || 
                                errorCodeStr.equals("EMAIL_EXISTS"))) {
                                return Mono.error(
                                        new ResponseStatusException(HttpStatus.CONFLICT, "Email ya registrado en Firebase."));
                            }
                        }
                    }

                    return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error interno del servidor."));
                });
    }

    @PostMapping("/login")
    public Mono<String> loginUser(@Valid @RequestBody LoginRequest request) {
        // System.out.println("Petición de login recibida para: " + request.getEmail());

        return authService.loginUser(request)
                .onErrorResume(e -> {
                    // Captura el error de credenciales inválidas que lanza el Repository
                    if (e.getMessage().contains("Credenciales inválidas")) {
                        return Mono
                                .error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas."));
                    }
                    // Manejo de errores genéricos (500)
                    return Mono.error(
                            new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al procesar login."));
                });
    }
}
