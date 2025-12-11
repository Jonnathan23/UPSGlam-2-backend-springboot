package com.main.spring.app.interfaces.users;

import com.main.spring.app.dto.UserSearchResponse;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository {
    Flux<UserSearchResponse> searchUsers(String query, int limit);

    Mono<com.main.spring.app.schema.UserSchema> getUserById(String userId);

    Mono<Void> updatePhotoUrl(String userId, String photoUrl);

    Mono<Void> updateBio(String userId, String bio);

    Mono<com.main.spring.app.schema.UserSchema> findUserByEmail(String email);
}
