package com.main.spring.app.repository;

import com.main.spring.app.dto.FirebaseTokenResponse;
import com.main.spring.app.interfaces.auth.AuthRepository;
import com.google.firebase.auth.FirebaseAuthException;
import com.main.spring.app.model.auth.RegisterRequest;
import com.main.spring.app.model.auth.LoginRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.http.MediaType;

import reactor.core.publisher.Mono;

import java.util.Map;

@Repository
public class AuthRepositoryImpl implements AuthRepository {

    private final FirebaseAuth firebaseAuth;
    private final WebClient webClient;
    private final String firebaseApiKey;

    public AuthRepositoryImpl(FirebaseAuth firebaseAuth,
            @Qualifier("firebaseAuthWebClient") WebClient firebaseAuthWebClient,
            @Value("${firebase.api.key}") String firebaseApiKey) {
        this.firebaseAuth = firebaseAuth;
        this.webClient = firebaseAuthWebClient;
        this.firebaseApiKey = firebaseApiKey;
    }

    @Override
    public Mono<String> registerUser(RegisterRequest request) {

        return Mono.fromCallable(() -> {
            try {
                UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                        .setEmail(request.getEmail())
                        .setPassword(request.getPassword())
                        .setDisplayName(request.getEmail().split("@")[0]); // Usar parte del email como nombre

                UserRecord userRecord = firebaseAuth.createUser(createRequest);

                // Retorna la UID de Firebase como el identificador de la cuenta
                return userRecord.getUid();
            } catch (FirebaseAuthException e) {
                // Log para debugging
                // System.out.println("FirebaseAuthException capturada - ErrorCode: " +
                // e.getErrorCode());
                // System.out.println("FirebaseAuthException - Mensaje: " + e.getMessage());

                // Capturar excepción específica de Firebase cuando el email ya existe
                // getErrorCode() retorna un enum, lo convertimos a String para comparar
                // Firebase usa ALREADY_EXISTS cuando el email ya existe
                String errorCodeStr = e.getErrorCode() != null ? e.getErrorCode().name() : null;
                if (errorCodeStr != null && (errorCodeStr.equals("ALREADY_EXISTS") ||
                        errorCodeStr.equals("EMAIL_EXISTS"))) {
                    throw new RuntimeException("EMAIL_ALREADY_EXISTS");
                }
                // Re-lanzar otras excepciones de Firebase
                throw new RuntimeException("Error de Firebase: " + e.getMessage(), e);
            } catch (Exception e) {
                // Capturar cualquier otra excepción y loggear
                // System.out.println("Excepción general en registerUser: " +
                // e.getClass().getName());
                // System.out.println("Mensaje: " + e.getMessage());
                if (e.getCause() != null) {
                    // System.out.println("Causa: " + e.getCause().getClass().getName());
                    // System.out.println("Causa mensaje: " + e.getCause().getMessage());
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

                .onStatus(s -> s.is4xxClientError(), response ->
                // El error 4xx indica credenciales incorrectas.
                Mono.error(new RuntimeException("CREDENCIALES_INVALIDAS")))

                // 4. Deserializamos la respuesta exitosa usando la clase pública
                .bodyToMono(FirebaseTokenResponse.class)
                // 5. Mapeamos para devolver solo el ID Token
                .map(response -> response.getIdToken());
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
