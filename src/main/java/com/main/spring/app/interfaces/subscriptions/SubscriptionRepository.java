package com.main.spring.app.interfaces.subscriptions;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SubscriptionRepository {
    Mono<Void> subscribe(String followerUid, String followingUid);
    Mono<Void> unsubscribe(String followerUid, String followingUid);
    Flux<String> getFollowing(String userId);
    Flux<String> getFollowers(String userId);
    Mono<Boolean> isSubscribed(String followerUid, String followingUid);
}

