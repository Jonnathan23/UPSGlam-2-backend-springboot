package com.main.spring.app.presentation.auth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final FirebaseAuth firebaseAuth;

    public AuthController(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> register() {
        // Generamos un email único para que la prueba funcione varias veces
        String testEmail = "testuser_" + System.currentTimeMillis() + "@upsglam.test";

        // Mono.fromCallable envuelve la llamada bloqueante de Firebase y la ejecuta
        // en un threadpool separado, manteniendo el Event Loop de WebFlux libre.
        return Mono.fromCallable(() -> {

            // 1. Crear la petición de usuario
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(testEmail)
                    .setPassword("password123") // Contraseña dummy
                    .setDisplayName("Test User");

            // 2. Ejecutar la llamada bloqueante a Firebase Auth
            UserRecord userRecord = firebaseAuth.createUser(request);

            // 3. Devolver la respuesta
            return "Usuario de prueba creado en Firebase. UID: " + userRecord.getUid();

        }).onErrorMap(e -> {
            // Manejo básico de errores de Firebase
            System.err.println("Error de Firebase: " + e.getMessage());
            if (e.getMessage().contains("EMAIL_ALREADY_EXISTS")) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya existe en Firebase.");
            }
            // Para otros errores (4xx, 5xx)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al crear usuario.");
        });
    }
}
