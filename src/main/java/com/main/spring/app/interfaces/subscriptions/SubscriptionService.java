package com.main.spring.app.interfaces.subscriptions;

import com.main.spring.app.dto.UserSearchResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SubscriptionService {
    Mono<String> subscribe(String followerUid, String followingUid);
    Mono<String> unsubscribe(String followerUid, String followingUid);
    Flux<UserSearchResponse> getFollowing(String userId);
    Flux<UserSearchResponse> getFollowers(String userId);
}

