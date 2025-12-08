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

        return authService.registerUser(request)
                .onErrorResume(e -> {
                    String message = e.getMessage();
                    if (message != null && message.contains("EMAIL_ALREADY_EXISTS")) {
                        return Mono.error(
                                new ResponseStatusException(HttpStatus.CONFLICT, "Email ya registrado en Firebase."));
                    }
                    
                    Throwable cause = e.getCause();
                    if (cause != null) {
                        String causeMessage = cause.getMessage();       

                        if (causeMessage != null && causeMessage.contains("EMAIL_ALREADY_EXISTS")) {
                            return Mono.error(
                                    new ResponseStatusException(HttpStatus.CONFLICT,
                                            "Email ya registrado en Firebase."));
                        }

                        if (cause instanceof com.google.firebase.auth.FirebaseAuthException) {
                            com.google.firebase.auth.FirebaseAuthException firebaseEx = (com.google.firebase.auth.FirebaseAuthException) cause;
                            String errorCodeStr = firebaseEx.getErrorCode() != null ? firebaseEx.getErrorCode().name()
                                    : null;
                            if (errorCodeStr != null && (errorCodeStr.equals("ALREADY_EXISTS") ||
                                    errorCodeStr.equals("EMAIL_EXISTS"))) {
                                return Mono.error(
                                        new ResponseStatusException(HttpStatus.CONFLICT,
                                                "Email ya registrado en Firebase."));
                            }
                        }
                    }

                    return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error interno del servidor."));
                });
    }

    

    @PostMapping("/login")
    public Mono<String> loginUser(@Valid @RequestBody LoginRequest request) {

        System.out.println("Petición de login recibida para: " + request.getEmail());
        return authService.loginUser(request)
                .onErrorResume(e -> {
                    String message = e.getMessage();

                    if (message != null && message.contains("CREDENCIALES_INVALIDAS")) {
                        return Mono
                                .error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas."));
                    }

                    // Errores de conexión con Firebase
                    if (message != null && message.contains("ERROR_CONEXION_FIREBASE")) {
                        System.err.println("Error de conexión con Firebase: " + message);
                        return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                                "No se puede conectar con el servicio de autenticación. Verifica tu conexión a internet."));
                    }

                    // Errores de timeout
                    if (message != null && message.contains("ERROR_TIMEOUT_FIREBASE")) {
                        System.err.println("Timeout al conectar con Firebase: " + message);
                        return Mono.error(new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT,
                                "El servicio de autenticación no respondió a tiempo. Intenta nuevamente."));
                    }

                    // Si el error es un fallo de WebClient o un 500
                    System.err.println("Error al contactar servicio o interno: " + message); // Log de debug
                    return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error interno del servidor al procesar login."));
                });
    }
}
