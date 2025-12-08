package com.main.spring.app.repository;

import com.main.spring.app.dto.FirebaseTokenResponse;
import com.main.spring.app.interfaces.auth.AuthRepository;
import com.google.firebase.auth.FirebaseAuthException;
import com.main.spring.app.model.auth.RegisterRequest;
import com.main.spring.app.schema.UserSchema;
import com.main.spring.app.model.auth.LoginRequest;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Map;

@Repository
public class AuthRepositoryImpl implements AuthRepository {

    private final FirebaseAuth firebaseAuth;
    private final WebClient webClient;
    private final String firebaseApiKey;
    private final Firestore firestoreDb;

    public AuthRepositoryImpl(
            FirebaseAuth firebaseAuth,
            @Qualifier("firebaseAuthWebClient") WebClient firebaseAuthWebClient,
            @Value("${firebase.api.key}") String firebaseApiKey,
            Firestore firestoreDb) {

        this.firebaseAuth = firebaseAuth;
        this.webClient = firebaseAuthWebClient;
        this.firebaseApiKey = firebaseApiKey;
        this.firestoreDb = firestoreDb;
    }

    @Override
    public Mono<String> registerUser(RegisterRequest request) {

        return Mono.fromCallable(() -> {
            try {
                UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                        .setEmail(request.getUsr_email())
                        .setPassword(request.getUsr_password())
                        .setDisplayName(request.getUsr_username());

                UserRecord userRecord = firebaseAuth.createUser(createRequest);
                String userUid = Objects.requireNonNull(userRecord.getUid(),
                        "UID de Firebase no puede ser nulo después de la creación.");

                UserSchema userProfile = new UserSchema(
                        request.getUsr_username(),
                        request.getUsr_email(),
                        request.getUsr_photoUrl(),
                        request.getUsr_bio());

                firestoreDb.collection("Users").document(userUid).set(userProfile).get();

                return "User registered successfully";
            } catch (FirebaseAuthException e) {
                String errorCodeStr = e.getErrorCode() != null ? e.getErrorCode().name() : null;
                if (errorCodeStr != null && (errorCodeStr.equals("ALREADY_EXISTS") ||
                        errorCodeStr.equals("EMAIL_EXISTS"))) {
                    throw new RuntimeException("EMAIL_ALREADY_EXISTS");
                }

                throw new RuntimeException("Error de Firebase: " + e.getMessage(), e);
            } catch (Exception e) {
                if (e.getCause() != null) {
                    System.out.println("Causa: " + e.getCause().getClass().getName());
                    System.out.println("Causa mensaje: " + e.getCause().getMessage());
                }
                throw e;
            }
        });
    }

    @Override
    public Mono<String> loginUser(LoginRequest request) {

        Map<String, String> body = Map.of(
                "email", request.getEmail(),
                "password", request.getPassword(),
                "returnSecureToken", "true");

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        // Endpoint correcto: accounts:signInWithPassword
                        .path("/accounts:signInWithPassword")
                        .queryParam("key", firebaseApiKey)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()

                .onStatus(s -> s.is4xxClientError(),
                        response -> Mono.error(new RuntimeException("CREDENCIALES_INVALIDAS")))

                .bodyToMono(FirebaseTokenResponse.class)

                .map(response -> response.getIdToken())
                
                // Manejo de errores de conexión/DNS
                .onErrorResume(e -> {
                    String errorMessage = e.getMessage();
                    if (errorMessage != null) {
                        // Error de DNS o conexión
                        if (errorMessage.contains("Failed to resolve") || 
                            errorMessage.contains("Name resolution") ||
                            errorMessage.contains("Connection refused") ||
                            errorMessage.contains("Network is unreachable")) {
                            throw new RuntimeException("ERROR_CONEXION_FIREBASE: No se puede conectar con Firebase. Verifica tu conexión a internet.");
                        }
                        // Error de timeout
                        if (errorMessage.contains("timeout") || errorMessage.contains("Timeout")) {
                            throw new RuntimeException("ERROR_TIMEOUT_FIREBASE: La conexión con Firebase tardó demasiado. Intenta nuevamente.");
                        }
                    }
                    // Re-lanzar otros errores
                    throw new RuntimeException("ERROR_FIREBASE: " + errorMessage, e);
                });
    }

    /**
     * Verifica la validez de un Token JWT usando el Admin SDK (para Spring
     * Security).
     * 
     * @param token ID Token JWT de Firebase.
     * @return Mono<String> que contiene el UID.
     */
    @Override
    public Mono<String> getUidFromToken(String token) {
        // Envolvemos la llamada bloqueante en un Mono (crucial para WebFlux)
        return Mono.fromCallable(() -> {
            try {
                // El Admin SDK verifica la firma del token con Google
                return firebaseAuth.verifyIdToken(token).getUid();
            } catch (FirebaseAuthException e) {
                // Lanza error si el token expiró o es inválido
                throw new org.springframework.security.authentication.BadCredentialsException(
                        "Token inválido o expirado");
            }
        });
    }

}
