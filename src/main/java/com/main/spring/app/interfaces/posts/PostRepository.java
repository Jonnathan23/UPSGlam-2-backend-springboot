package com.main.spring.app.interfaces.posts;

import org.springframework.http.codec.multipart.FilePart;

import com.main.spring.app.schema.PostsSchema;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PostRepository {
    Mono<String> createPost(FilePart filePart, String caption, String authorUid);
    Mono<Void> updateLikeCount(String postId, int increment);
    Mono<Void> updateCommentCount(String postId, int increment);
    Flux<PostsSchema> getPostsByAuthor(String authorUid);
    Mono<PostsSchema> getPostById(String postId);
    Mono<Void> deletePost(String postId);
}
