package com.main.spring.app.interfaces.comments;

import reactor.core.publisher.Mono;

public interface CommentsRepository {
    Mono<String> createComment(String postId, String authorUid, String text);
}
