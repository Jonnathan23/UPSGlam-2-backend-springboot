package com.main.spring.app.service;

import com.main.spring.app.interfaces.auth.AuthRepository;
import com.main.spring.app.interfaces.auth.AuthService;
import com.main.spring.app.model.RegisterRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthServiceImpl implements AuthService {
    private final AuthRepository authRepository;

    public AuthServiceImpl(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public Mono<String> registerUser(RegisterRequest request) {
        // 1. Crear el usuario en Firebase Auth (Llamada al Repositorio)
        return authRepository.registerUser(request)
                .flatMap(uid -> {
                    // 2. Tarea de Negocio Adicional: Guardar el perfil inicial en Firestore
                    // Aquí devolveremos el token (simulado) o un mensaje de éxito.
                    System.out.println("LOG: Usuario creado en Firebase Auth. UID: " + uid);
                    // Ejemplo de una llamada a Firestore (no implementado aún):
                    // return authRepository.saveProfileData(uid,
                    // request).thenReturn("token_generado");
                    return Mono.just("Registro exitoso. UID: " + uid);
                });
    }

}
