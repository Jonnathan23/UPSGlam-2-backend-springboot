package com.main.spring.app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.main.spring.app.interfaces.likes.LikeService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/posts")
public class LikeControllerImpl {
    private final LikeService likeService;

    public LikeControllerImpl(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/{postId}/likes")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> createLike(
            @PathVariable String postId,
            Authentication authentication // JWT validado
    ) {
        String authorUid = (String) authentication.getPrincipal();

        return likeService.createLike(postId, authorUid) // Llamada al servicio
                .onErrorResume(e -> {
                    String message = e.getMessage();

                    if (message != null && message.contains("ALREADY_LIKED")) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.CONFLICT, "El usuario ya ha dado like a esta publicación."));
                    }

                    System.err.println("ERROR: Fallo al crear el like. Causa: " + message);
                    return Mono.error(new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al procesar el like."));
                });
    }

}
