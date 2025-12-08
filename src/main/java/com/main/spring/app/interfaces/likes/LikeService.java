package com.main.spring.app.interfaces.likes;

import reactor.core.publisher.Mono;

public interface LikeService {
    Mono<String> createLike(String postId, String authorUid);
    
}
