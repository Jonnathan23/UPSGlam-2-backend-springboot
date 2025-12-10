package com.main.spring.app.interfaces.users;

import com.main.spring.app.dto.UserSearchResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository {
    Flux<UserSearchResponse> searchUsers(String query, int limit);
    Mono<com.main.spring.app.schema.UserSchema> getUserById(String userId);
}

