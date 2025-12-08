package com.main.spring.app.repository;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.main.spring.app.interfaces.likes.LikeRepository;

import reactor.core.publisher.Mono;

@Repository
public class LikeRepositoryImpl implements LikeRepository {

    private final Firestore firestoreDb;

    public LikeRepositoryImpl(Firestore firestoreDb) {
        this.firestoreDb = firestoreDb;
    }

    @Override
    public Mono<String> createLike(String postId, String authorUid) {

        return Mono.fromCallable(() -> {

            DocumentReference likeRef = firestoreDb.collection("posts")
                    .document(java.util.Objects.requireNonNull(postId, "postId no puede ser null"))
                    .collection("likes")
                    .document(java.util.Objects.requireNonNull(authorUid, "authorUid no puede ser null"));

            if (likeRef.get().get().exists()) {
                throw new RuntimeException("ALREADY_LIKED");
            }

            Map<String, Object> likeData = new HashMap<>();
            likeData.put("lik_timestamp", Timestamp.now());
            likeData.put("lik_authorUid", authorUid);
            likeData.put("lik_postUid", postId);

            likeRef.set(likeData).get();

            return "Like creado exitosamente";

        }).onErrorMap(e -> {
            if (e instanceof RuntimeException && "ALREADY_LIKED".equals(e.getMessage())) {
                return e;
            }
            System.err.println("Error de Firestore al crear like: " + e.getMessage());
            return new RuntimeException("FIRESTORE_LIKE_FAILED", e);
        });
    }
}
