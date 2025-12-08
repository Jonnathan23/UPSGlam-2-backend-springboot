package com.main.spring.app.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Repository;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.main.spring.app.interfaces.likes.LikeRepository;
import com.main.spring.app.schema.LikeSchema;

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

            DocumentReference likeRef = firestoreDb.collection("Posts") 
                    .document(Objects.requireNonNull(postId, "postId no puede ser null"))
                    .collection("Likes") 
                    .document(Objects.requireNonNull(authorUid, "authorUid no puede ser null"));

            if (likeRef.get().get().exists()) {
                throw new RuntimeException("ALREADY_LIKED");
            }

            LikeSchema likeData = new LikeSchema(
                authorUid, 
                postId     
            );

            // GUARDAR EL OBJETO POJO COMPLETO EN FIRESTORE
            likeRef.set(likeData).get(); 

            // 🚨 CORRECCIÓN: Retornamos el mensaje de éxito 🚨
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
