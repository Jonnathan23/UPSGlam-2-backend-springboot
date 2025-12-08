package com.main.spring.app.controller;

import com.main.spring.app.interfaces.comments.CommentsService;
import com.main.spring.app.model.CommentRequest;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/posts")
public class CommentController {

    private final CommentsService commentsService;

    public CommentController(CommentsService commentsService) {
        this.commentsService = commentsService;
    }

    @PostMapping("/{postId}/comments") // 👈 Endpoint para Comentar
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> createComment(
            @PathVariable String postId,
            @Valid @RequestBody CommentRequest request, // 👈 Recibe el body {"com_text": "string"}
            Authentication authentication // JWT validado
    ) {
        // 1. Extraer UID del autor del JWT
        String authorUid = (String) authentication.getPrincipal();
        String text = request.getCom_text();

        // 2. Delegar al Service para crear el comentario
        return commentsService.createComment(postId, authorUid, text)
                .onErrorResume(e -> {
                    System.err.println("ERROR: Fallo al crear el comentario. Causa: " + e.getMessage());
                    return Mono.error(new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al procesar el comentario."));
                });
    }


    
}
