package com.main.spring.app.service;

import com.main.spring.app.interfaces.likes.LikeRepository;
import com.main.spring.app.interfaces.likes.LikeService;
import com.main.spring.app.interfaces.posts.PostRepository;

import reactor.core.publisher.Mono;

import org.springframework.stereotype.Service;

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
                .flatMap(message -> {
                    int increment = (message.contains("creado")) ? 1 : -1;
                    return postRepository
                            .updateLikeCount(postId, increment)
                            .thenReturn(message);
                })
                .doOnSuccess(
                        message -> System.out.println("LOG: " + message + " y contador actualizado en post " + postId))
                .onErrorResume(e ->
                // Propaga el error del repositorio (incluyendo ALREADY_LIKED)
                Mono.error(e));
    }
}
