package com.main.spring.app.interfaces.users;

import com.main.spring.app.dto.UserSearchResponse;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {
    Flux<UserSearchResponse> searchUsers(String query);
    Mono<String> updatePhoto(String userId, FilePart filePart);
    Mono<String> updateBio(String userId, String bio);
}

