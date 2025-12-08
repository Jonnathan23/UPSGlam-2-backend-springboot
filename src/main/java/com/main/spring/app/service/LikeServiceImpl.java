package com.main.spring.app.service;

import com.main.spring.app.interfaces.likes.LikeRepository;
import com.main.spring.app.interfaces.likes.LikeService;

import reactor.core.publisher.Mono;

import org.springframework.stereotype.Service;

@Service
public class LikeServiceImpl implements LikeService {
    private final LikeRepository likeRepository;

    public LikeServiceImpl(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    @Override
    public Mono<String> createLike(String postId, String authorUid) {

        return likeRepository.createLike(postId, authorUid) // 👈 Llama al repositorio
                .doOnSuccess(likeId -> System.out.println("LOG: Like creado exitosamente en post " + postId))
                .onErrorResume(e -> Mono.error(e));
    }
}
