package com.main.spring.app.controller;

import com.main.spring.app.interfaces.subscriptions.SubscriptionService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/{userId}/subscribe")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> subscribe(
            @PathVariable String userId,
            Authentication authentication // JWT validado
    ) {
        // 1. Extraer UID del JWT
        String followerUid = (String) authentication.getPrincipal();

        // 2. Delegar al servicio
        return subscriptionService.subscribe(followerUid, userId);
    }

    @DeleteMapping("/{userId}/subscribe")
    @ResponseStatus(HttpStatus.OK)
    public Mono<String> unsubscribe(
            @PathVariable String userId,
            Authentication authentication // JWT validado
    ) {
        // 1. Extraer UID del JWT
        String followerUid = (String) authentication.getPrincipal();

        // 2. Delegar al servicio
        return subscriptionService.unsubscribe(followerUid, userId);
    }
}

