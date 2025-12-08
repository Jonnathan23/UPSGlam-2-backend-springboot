package com.main.spring.app.interfaces;

import org.springframework.http.codec.multipart.FilePart;

import reactor.core.publisher.Mono;

public interface PostRepository {
    Mono<String> createPost(FilePart filePart, String caption, String authorUid);
}
