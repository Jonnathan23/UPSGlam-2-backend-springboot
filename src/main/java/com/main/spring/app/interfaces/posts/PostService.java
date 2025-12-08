package com.main.spring.app.interfaces.posts;

import org.springframework.http.codec.multipart.FilePart;

import reactor.core.publisher.Mono;

public interface PostService {
    Mono<String> createPost(FilePart filePart, String caption, String authorUid);

}
