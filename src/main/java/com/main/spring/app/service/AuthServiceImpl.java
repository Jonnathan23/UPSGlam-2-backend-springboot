package com.main.spring.app.service;

import com.main.spring.app.interfaces.auth.AuthRepository;
import com.main.spring.app.interfaces.auth.AuthService;
import com.main.spring.app.model.auth.LoginRequest;
import com.main.spring.app.model.auth.RegisterRequest;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthServiceImpl implements AuthService {
    private final AuthRepository authRepository;

    public AuthServiceImpl(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    @Override
    public Mono<String> registerUser(RegisterRequest request) {
        return authRepository.registerUser(request)
                .flatMap(uid -> {
                    System.out.println("LOG: Usuario creado en Firebase Auth. UID: " + uid);
                    return Mono.just("Registro exitoso. UID: " + uid);
                });
    }

    @Override
    public Mono<String> loginUser(LoginRequest request) {
        // Lógica de negocio: simplemente llama al repositorio para obtener el token
        return authRepository.loginUser(request)
                .doOnSuccess(token -> System.out.println("LOG: Login exitoso. Token generado."))
                .onErrorResume(e -> Mono.error(new RuntimeException("Error en login: " + e.getMessage())));
    }

}
