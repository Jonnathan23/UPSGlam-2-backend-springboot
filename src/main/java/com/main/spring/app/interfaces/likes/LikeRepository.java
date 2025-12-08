package com.main.spring.app.interfaces.likes;

import reactor.core.publisher.Mono;

public interface LikeRepository {
    Mono<String> createLike(String postId, String authorUid);
}
