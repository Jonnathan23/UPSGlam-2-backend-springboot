package com.main.spring.app.interfaces.users;

import com.main.spring.app.dto.UserSearchResponse;
import reactor.core.publisher.Flux;

public interface UserService {
    Flux<UserSearchResponse> searchUsers(String query);
}

