package com.main.spring.app.service;

import org.springframework.stereotype.Service;

import com.main.spring.app.dto.UserSearchResponse;
import com.main.spring.app.interfaces.users.UserRepository;
import com.main.spring.app.interfaces.users.UserService;

import reactor.core.publisher.Flux;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private static final int SEARCH_LIMIT = 5;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Flux<UserSearchResponse> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Flux.empty();
        }

        return userRepository.searchUsers(query.trim(), SEARCH_LIMIT)
                .onErrorResume(e -> {
                    System.err.println("ERROR: Fallo al buscar usuarios. Causa: " + e.getMessage());
                    return Flux.empty();
                });
    }
}

