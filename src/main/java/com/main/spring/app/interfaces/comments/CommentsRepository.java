package com.main.spring.app.interfaces.comments;

import com.main.spring.app.schema.CommenSchema;
import reactor.core.publisher.Mono;

public interface CommentsRepository {
    Mono<String> createComment(String postId, String authorUid, String text);
    Mono<Void> deleteComment(String postId, String commentId);
    Mono<CommenSchema> getCommentById(String postId, String commentId);
}
