package com.main.spring.app.service;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.main.spring.app.dto.UserSearchResponse;
import com.main.spring.app.interfaces.subscriptions.SubscriptionRepository;
import com.main.spring.app.interfaces.subscriptions.SubscriptionService;
import com.main.spring.app.interfaces.users.UserRepository;

import org.springframework.http.HttpStatus;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository, UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Mono<String> subscribe(String followerUid, String followingUid) {
        // 1. Validar que no sea auto-suscripción
        if (followerUid.equals(followingUid)) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "No puedes suscribirte a ti mismo."));
        }

        // 2. Verificar que el usuario a seguir exista
        return userRepository.getUserById(followingUid)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El usuario a seguir no existe.")))
                .flatMap(user -> {
                    // 3. Verificar que no esté ya suscrito
                    return subscriptionRepository.isSubscribed(followerUid, followingUid)
                            .flatMap(isSubscribed -> {
                                if (isSubscribed) {
                                    return Mono.error(new ResponseStatusException(
                                            HttpStatus.CONFLICT, "Ya estás suscrito a este usuario."));
                                }

                                // 4. Crear la relación bidireccional
                                return subscriptionRepository.subscribe(followerUid, followingUid)
                                        .thenReturn("Suscripción creada correctamente")
                                        .doOnSuccess(message -> System.out.println(
                                                "LOG: Usuario " + followerUid + " se suscribió a " + followingUid))
                                        .onErrorResume(e -> {
                                            System.err.println("ERROR: Fallo al crear suscripción. Causa: " + e.getMessage());
                                            return Mono.error(new ResponseStatusException(
                                                    HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al crear la suscripción."));
                                        });
                            });
                });
    }

    @Override
    public Mono<String> unsubscribe(String followerUid, String followingUid) {
        // 1. Validar que no sea auto-desuscripción (aunque técnicamente no debería pasar)
        if (followerUid.equals(followingUid)) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "No puedes desuscribirte de ti mismo."));
        }

        // 2. Verificar que la suscripción existe
        return subscriptionRepository.isSubscribed(followerUid, followingUid)
                .flatMap(isSubscribed -> {
                    if (!isSubscribed) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "No estás suscrito a este usuario."));
                    }

                    // 3. Eliminar la relación bidireccional
                    return subscriptionRepository.unsubscribe(followerUid, followingUid)
                            .thenReturn("Suscripción eliminada correctamente")
                            .doOnSuccess(message -> System.out.println(
                                    "LOG: Usuario " + followerUid + " se desuscribió de " + followingUid))
                            .onErrorResume(e -> {
                                System.err.println("ERROR: Fallo al eliminar suscripción. Causa: " + e.getMessage());
                                return Mono.error(new ResponseStatusException(
                                        HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al eliminar la suscripción."));
                            });
                });
    }

    @Override
    public Flux<UserSearchResponse> getFollowing(String userId) {
        // 1. Obtener lista de UIDs que sigue el usuario
        return subscriptionRepository.getFollowing(userId)
                .flatMap(followingUid -> {
                    // 2. Para cada UID, obtener los datos completos del usuario
                    return userRepository.getUserById(followingUid)
                            .map(user -> new UserSearchResponse(
                                    followingUid,
                                    user.getUsr_username(),
                                    user.getUsr_email(),
                                    user.getUsr_photoUrl()
                            ))
                            .onErrorResume(e -> {
                                // Si el usuario no existe, retornar null y filtrar después
                                System.err.println("ADVERTENCIA: Usuario " + followingUid + " no encontrado");
                                return Mono.empty();
                            });
                })
                .onErrorResume(e -> {
                    System.err.println("ERROR: Fallo al obtener following. Causa: " + e.getMessage());
                    return Flux.empty();
                });
    }

    @Override
    public Flux<UserSearchResponse> getFollowers(String userId) {
        // 1. Obtener lista de UIDs que siguen al usuario
        return subscriptionRepository.getFollowers(userId)
                .flatMap(followerUid -> {
                    // 2. Para cada UID, obtener los datos completos del usuario
                    return userRepository.getUserById(followerUid)
                            .map(user -> new UserSearchResponse(
                                    followerUid,
                                    user.getUsr_username(),
                                    user.getUsr_email(),
                                    user.getUsr_photoUrl()
                            ))
                            .onErrorResume(e -> {
                                // Si el usuario no existe, retornar null y filtrar después
                                System.err.println("ADVERTENCIA: Usuario " + followerUid + " no encontrado");
                                return Mono.empty();
                            });
                })
                .onErrorResume(e -> {
                    System.err.println("ERROR: Fallo al obtener followers. Causa: " + e.getMessage());
                    return Flux.empty();
                });
    }
}

