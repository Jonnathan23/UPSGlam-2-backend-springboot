package com.main.spring.app.presentation.auth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> register() {
        System.out.println("register");
        return Mono.just("Recibido el cuerpo de la petición");
    }
}
