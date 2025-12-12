package com.main.spring.app.service;

import com.main.spring.app.interfaces.likes.LikeRepository;
import com.main.spring.app.interfaces.likes.LikeService;
import com.main.spring.app.interfaces.posts.PostRepository;

import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class LikeServiceImpl implements LikeService {
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;

    public LikeServiceImpl(LikeRepository likeRepository, PostRepository postRepository) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
    }

    @Override
    public Mono<String> createLike(String postId, String authorUid) {

        return likeRepository.createLike(postId, authorUid) // 1. CREAR EL DOCUMENTO LIKE
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(TimeoutException.class,
                        e -> Mono.error(new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT,
                                "El servicio de likes tardó demasiado en responder.")))
                .flatMap(message -> {
                    int increment = (message.contains("creado")) ? 1 : -1;
                    return postRepository
                            .updateLikeCount(postId, increment)
                            .thenReturn(message);
                })
                .doOnSuccess(
                        message -> System.out.println("LOG: " + message + " y contador actualizado en post " + postId))
                .onErrorResume(e -> {
                    System.err.println("ERROR: Fallo al procesar like. Causa: " + e.getMessage());
                    return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error interno al procesar el like.", e));
                });
    }
}
