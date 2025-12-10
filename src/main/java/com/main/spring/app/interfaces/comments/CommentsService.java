package com.main.spring.app.interfaces.comments;

import reactor.core.publisher.Mono;

public interface CommentsService {
    Mono<String> createComment(String postId, String authorUid, String text);
    Mono<String> deleteComment(String postId, String commentId, String authorUid);
}
