package com.main.spring.app.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.main.spring.app.interfaces.auth.AuthRepository;
import com.main.spring.app.model.auth.LoginRequest;
import com.main.spring.app.model.auth.RegisterRequest;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class AuthRepositoryImpl implements AuthRepository {

    private final FirebaseAuth firebaseAuth;

    public AuthRepositoryImpl(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
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
                System.out.println("FirebaseAuthException capturada - ErrorCode: " + e.getErrorCode());
                System.out.println("FirebaseAuthException - Mensaje: " + e.getMessage());
                
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
                System.out.println("Excepción general en registerUser: " + e.getClass().getName());
                System.out.println("Mensaje: " + e.getMessage());
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
        // Envolvemos la llamada bloqueante en un Mono
        return Mono.fromCallable(() -> {

            try {                
                UserRecord user = firebaseAuth.getUserByEmail(request.getEmail());

                String customToken = firebaseAuth.createCustomToken(user.getUid());

                return customToken;

            } catch (Exception e) {
                // Capturamos cualquier excepción (ej: usuario no encontrado)
                // y relanzamos un error para que el Controller lo maneje.
                throw new RuntimeException("Credenciales inválidas o usuario no existe.");
            }
        });
    }

    @Override
    public Mono<String> getUidFromToken(String token) {
        // Necesario para el flujo de seguridad, pero dejaremos el cuerpo vacío por
        // ahora.
        throw new UnsupportedOperationException("Unimplemented method 'getUidFromToken'");
    }

}
