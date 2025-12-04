package com.main.spring.app.interfaces.auth;

import com.main.spring.app.model.RegisterRequest;

import reactor.core.publisher.Mono;

public interface AuthRepository {   
    Mono<String> registerUser(RegisterRequest request);
    //Todo: Mono<String> loginUser(LoginRequest request);
}
