package com.main.spring.app.interfaces.posts;

import org.springframework.http.codec.multipart.FilePart;

import com.main.spring.app.schema.PostsSchema;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PostService {
    Mono<String> createPost(FilePart filePart, String caption, String authorUid);
    Flux<PostsSchema> getPostsByAuthor(String authorUid);
    Mono<String> deletePost(String postId, String authorUid);
}
