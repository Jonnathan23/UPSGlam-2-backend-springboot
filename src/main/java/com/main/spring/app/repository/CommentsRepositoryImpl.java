package com.main.spring.app.repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.main.spring.app.interfaces.comments.CommentsRepository;
import com.main.spring.app.schema.CommenSchema;

import reactor.core.publisher.Mono;

import java.util.Objects;

import org.springframework.stereotype.Repository;

@Repository
public class CommentsRepositoryImpl implements CommentsRepository {

    private final Firestore firestoreDb;

    public CommentsRepositoryImpl(Firestore firestoreDb) {
        this.firestoreDb = firestoreDb;
    }

    @Override
    public Mono<String> createComment(String postId, String authorUid, String text) {

        return Mono.fromCallable(() -> {

            // 1. Definir la referencia a la subcolección 'Comments' (Firestore generará el
            // ID)
            // Ruta: Posts/{postId}/Comments
            DocumentReference commentsCollectionRef = firestoreDb.collection("Posts") // Colección Raíz
                    .document(Objects.requireNonNull(postId, "postId no puede ser null"))
                    .collection("Comments") // Subcolección
                    .document(); // Firestore generará el ID del documento

            // 2. Crear el objeto Schema
            CommenSchema commentData = new CommenSchema(
                    authorUid,
                    text,
                    postId // com_posUid
            );

            // 3. GUARDAR EL OBJETO POJO COMPLETO EN FIRESTORE (Bloqueante)
            commentsCollectionRef.set(commentData).get();

            // 4. Retornamos el ID del documento recién creado
            return "Comentario creado exitosamente con ID";

        }).onErrorMap(e -> {
            System.err.println("Error de Firestore al crear comentario: " + e.getMessage());
            return new RuntimeException("FIRESTORE_COMMENT_FAILED", e);
        });
    }

}
