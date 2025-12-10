package com.main.spring.app.repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.main.spring.app.interfaces.subscriptions.SubscriptionRepository;
import com.main.spring.app.schema.SubscriptionSchema;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Repository;

@Repository
public class SubscriptionRepositoryImpl implements SubscriptionRepository {

    private final Firestore firestoreDb;

    public SubscriptionRepositoryImpl(Firestore firestoreDb) {
        this.firestoreDb = firestoreDb;
    }

    @Override
    public Mono<Void> subscribe(String followerUid, String followingUid) {
        return Mono.fromCallable(() -> {
            // 1. Crear documento en Following del follower
            // Ruta: Users/{followerUid}/Following/{followingUid}
            DocumentReference followingRef = firestoreDb.collection("Users")
                    .document(Objects.requireNonNull(followerUid, "followerUid no puede ser null"))
                    .collection("Following")
                    .document(Objects.requireNonNull(followingUid, "followingUid no puede ser null"));

            SubscriptionSchema followingData = new SubscriptionSchema();
            followingRef.set(followingData).get();

            // 2. Crear documento en Followers del following
            // Ruta: Users/{followingUid}/Followers/{followerUid}
            DocumentReference followersRef = firestoreDb.collection("Users")
                    .document(followingUid)
                    .collection("Followers")
                    .document(followerUid);

            SubscriptionSchema followersData = new SubscriptionSchema();
            followersRef.set(followersData).get();

            return null;
        }).onErrorMap(e -> {
            System.err.println("Error de Firestore al crear suscripción: " + e.getMessage());
            return new RuntimeException("FIRESTORE_SUBSCRIPTION_FAILED", e);
        }).then();
    }

    @Override
    public Mono<Void> unsubscribe(String followerUid, String followingUid) {
        return Mono.fromCallable(() -> {
            // 1. Eliminar documento en Following del follower
            DocumentReference followingRef = firestoreDb.collection("Users")
                    .document(Objects.requireNonNull(followerUid, "followerUid no puede ser null"))
                    .collection("Following")
                    .document(Objects.requireNonNull(followingUid, "followingUid no puede ser null"));

            followingRef.delete().get();

            // 2. Eliminar documento en Followers del following
            DocumentReference followersRef = firestoreDb.collection("Users")
                    .document(followingUid)
                    .collection("Followers")
                    .document(followerUid);

            followersRef.delete().get();

            return null;
        }).onErrorMap(e -> {
            System.err.println("Error de Firestore al eliminar suscripción: " + e.getMessage());
            return new RuntimeException("FIRESTORE_UNSUBSCRIPTION_FAILED", e);
        }).then();
    }

    @Override
    public Flux<String> getFollowing(String userId) {
        return Mono.fromCallable(() -> {
            QuerySnapshot snapshot = firestoreDb.collection("Users")
                    .document(Objects.requireNonNull(userId, "userId no puede ser null"))
                    .collection("Following")
                    .get()
                    .get();

            List<String> followingIds = snapshot.getDocuments()
                    .stream()
                    .map(document -> document.getId())
                    .toList();

            return followingIds;
        }).onErrorMap(e -> {
            System.err.println("Error de Firestore al obtener following: " + e.getMessage());
            return new RuntimeException("FIRESTORE_GET_FOLLOWING_FAILED", e);
        }).flatMapIterable(followingIds -> followingIds);
    }

    @Override
    public Flux<String> getFollowers(String userId) {
        return Mono.fromCallable(() -> {
            QuerySnapshot snapshot = firestoreDb.collection("Users")
                    .document(Objects.requireNonNull(userId, "userId no puede ser null"))
                    .collection("Followers")
                    .get()
                    .get();

            List<String> followerIds = snapshot.getDocuments()
                    .stream()
                    .map(document -> document.getId())
                    .toList();

            return followerIds;
        }).onErrorMap(e -> {
            System.err.println("Error de Firestore al obtener followers: " + e.getMessage());
            return new RuntimeException("FIRESTORE_GET_FOLLOWERS_FAILED", e);
        }).flatMapIterable(followerIds -> followerIds);
    }

    @Override
    public Mono<Boolean> isSubscribed(String followerUid, String followingUid) {
        return Mono.fromCallable(() -> {
            DocumentReference followingRef = firestoreDb.collection("Users")
                    .document(Objects.requireNonNull(followerUid, "followerUid no puede ser null"))
                    .collection("Following")
                    .document(Objects.requireNonNull(followingUid, "followingUid no puede ser null"));

            var documentSnapshot = followingRef.get().get();
            return documentSnapshot.exists();
        }).onErrorMap(e -> {
            System.err.println("Error de Firestore al verificar suscripción: " + e.getMessage());
            return new RuntimeException("FIRESTORE_CHECK_SUBSCRIPTION_FAILED", e);
        });
    }
}

