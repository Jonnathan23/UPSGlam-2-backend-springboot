package com.main.spring.app.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.main.spring.app.interfaces.auth.AuthRepository;
import com.main.spring.app.model.auth.LoginRequest;
import com.main.spring.app.model.auth.RegisterRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Repository
public class AuthRepositoryImpl implements AuthRepository {

    private final FirebaseAuth firebaseAuth;
    private final WebClient webClient;

    @Value("${firebase.api.key}")
    private String firebaseApiKey;

    public AuthRepositoryImpl(FirebaseAuth firebaseAuth, WebClient.Builder webClientBuilder) {
        this.firebaseAuth = firebaseAuth;
        this.webClient = webClientBuilder.build();
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
        return Mono.fromCallable(() -> {
            try {
                UserRecord user = firebaseAuth.getUserByEmail(request.getEmail());
                return firebaseAuth.createCustomToken(user.getUid());
            } catch (Exception e) {
                throw new RuntimeException("Credenciales inválidas o usuario no existe.");
            }
        }).flatMap(customToken -> exchangeCustomTokenForIdToken(customToken));
    }

    private Mono<String> exchangeCustomTokenForIdToken(String customToken) {
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithCustomToken?key=" + firebaseApiKey;

        return webClient.post()
                .uri(url)
                .bodyValue(Map.of("token", customToken, "returnSecureToken", true))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("idToken"))
                .onErrorMap(e -> new RuntimeException("Error al intercambiar token: " + e.getMessage()));
    }

    @Override
    public Mono<String> getUidFromToken(String token) {
        // Necesario para el flujo de seguridad, pero dejaremos el cuerpo vacío por
        // ahora.
        throw new UnsupportedOperationException("Unimplemented method 'getUidFromToken'");
    }

}
