package com.main.spring.app.interfaces.auth;

import com.main.spring.app.model.auth.LoginRequest;
import com.main.spring.app.model.auth.RegisterRequest;

import reactor.core.publisher.Mono;

public interface AuthRepository {   
    Mono<String> registerUser(RegisterRequest request);
    
    Mono<String> loginUser(LoginRequest request);

    Mono<String> getUidFromToken(String token);
}
