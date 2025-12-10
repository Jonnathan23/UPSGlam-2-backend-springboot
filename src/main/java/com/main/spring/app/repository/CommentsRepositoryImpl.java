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
            String commentId = commentsCollectionRef.getId();
            return commentId;

        }).onErrorMap(e -> {
            System.err.println("Error de Firestore al crear comentario: " + e.getMessage());
            return new RuntimeException("FIRESTORE_COMMENT_FAILED", e);
        });
    }

    @Override
    public Mono<CommenSchema> getCommentById(String postId, String commentId) {
        return Mono.fromCallable(() -> {
            DocumentReference commentRef = firestoreDb.collection("Posts")
                    .document(Objects.requireNonNull(postId, "postId no puede ser null"))
                    .collection("Comments")
                    .document(Objects.requireNonNull(commentId, "commentId no puede ser null"));

            var documentSnapshot = commentRef.get().get();
            
            if (!documentSnapshot.exists()) {
                return null;
            }

            return documentSnapshot.toObject(CommenSchema.class);
        }).onErrorMap(e -> {
            System.err.println("Error de Firestore al obtener comentario por ID: " + e.getMessage());
            return new RuntimeException("FIRESTORE_GET_COMMENT_FAILED", e);
        });
    }

    @Override
    public Mono<Void> deleteComment(String postId, String commentId) {
        return Mono.fromCallable(() -> {
            DocumentReference commentRef = firestoreDb.collection("Posts")
                    .document(Objects.requireNonNull(postId, "postId no puede ser null"))
                    .collection("Comments")
                    .document(Objects.requireNonNull(commentId, "commentId no puede ser null"));

            // Eliminar el documento de Firestore
            commentRef.delete().get();
            return null;
        }).onErrorMap(e -> {
            System.err.println("Error de Firestore al eliminar comentario: " + e.getMessage());
            return new RuntimeException("FIRESTORE_DELETE_COMMENT_FAILED", e);
        }).then();
    }

}
