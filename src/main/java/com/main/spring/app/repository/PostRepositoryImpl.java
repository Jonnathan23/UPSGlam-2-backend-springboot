package com.main.spring.app.repository;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.codec.multipart.FilePart;

import com.google.cloud.firestore.Firestore;
import com.main.spring.app.interfaces.posts.PostRepository;
import com.main.spring.app.schema.PostsSchema;

import com.google.cloud.firestore.DocumentReference; // Necesario
import com.google.cloud.firestore.FieldValue;

import reactor.core.publisher.Mono;

import org.springframework.stereotype.Repository;
import java.util.Objects;

@Repository
public class PostRepositoryImpl implements PostRepository {

    private final Firestore firestoreDb;

    public PostRepositoryImpl(Firestore firestoreDb) {
        this.firestoreDb = firestoreDb;
    }

    @Override
    public Mono<String> createPost(FilePart filePart, String caption, String authorUid) {

        // 1. Ejecutar el código bloqueante (guardar imagen) dentro de un
        // Mono.fromCallable
        // Esto devuelve un Mono<String> que contiene la URL de la imagen.
        Mono<String> imageUrlMono = Mono.fromCallable(() -> {
            // 🚨 Esta es la llamada bloqueante al método delegado 🚨
            return saveImageInBucket(filePart);
        });

        // 2. Encadenar la URL obtenida para crear el PostSchema y guardarlo en
        // Firestore
        return imageUrlMono.flatMap(imageUrl -> Mono.fromCallable(() -> {

            // Usamos el constructor que creamos en PostsSchema
            PostsSchema newPost = new PostsSchema(
                    authorUid,
                    imageUrl, // URL obtenida del bucket
                    caption);

            // 3. Obtener la referencia a la colección 'posts'
            // NOTA: Firestore no necesita que incluyas timestamp en el constructor si
            // usamos Timestamp.now() en el Schema
            DocumentReference docRef = firestoreDb.collection("Posts").document();

            // 4. Guardar el objeto POJO (Bloqueante)
            // .set() guarda el objeto, y .get() hace que la operación sea síncrona
            docRef.set(newPost).get();

            // 5. Retornar el ID del documento recién creado
            return docRef.getId();

        }));
    }

    private String saveImageInBucket(FilePart filePart) {
        // TODO: guardar la imagen en el bucket
        return "http://..";
    }

    @Override
    public Mono<Void> updateLikeCount(String postId, int increment) {

        return Mono.fromCallable(() -> {

            // 1. Obtener la referencia al documento Post principal
            DocumentReference postRef = firestoreDb.collection("Posts")
                    .document(Objects.requireNonNull(postId, "postId no puede ser null"));

            // 2. Crear un mapa para la actualización atómica
            Map<String, Object> update = Objects.requireNonNull(
                    Collections.singletonMap(
                            "pos_likesCount",
                            FieldValue.increment(increment) // Incrementar el valor en la DB
            ), "update map no puede ser null");

            // 3. Ejecutar la actualización (Bloqueante)
            postRef.update(update).get();

            return null; // Devolvemos Mono<Void>

        }).onErrorResume(e -> {
            System.err.println("ERROR FIRESTORE: Fallo al actualizar contador de Likes para post " + postId
                    + ". Causa: " + e.getMessage());
            // Devolvemos Mono.empty() o un error si la falla es crítica
            return Mono.error(new RuntimeException("FIRESTORE_UPDATE_FAILED"));
        }).then(); // Convertir el resultado a Mono<Void>
    }

    @Override
    public Mono<Void> updateCommentCount(String postId, int increment) {

        return Mono.fromCallable(() -> {

            // 1. Obtener la referencia al documento Post principal
            DocumentReference postRef = firestoreDb.collection("Posts")
                    .document(Objects.requireNonNull(postId, "postId no puede ser null"));

            // 2. Crear un mapa para la actualización atómica
            Map<String, Object> update = Objects.requireNonNull(
                    Collections.singletonMap(
                            "pos_commentsCount",
                            FieldValue.increment(increment) // Incrementar el valor en la DB
            ), "update map no puede ser null");

            // 3. Ejecutar la actualización (Bloqueante)
            postRef.update(update).get();

            return null; // Devolvemos Mono<Void>

        }).onErrorResume(e -> {
            System.err.println("ERROR FIRESTORE: Fallo al actualizar contador de Comentarios para post " + postId
                    + ". Causa: " + e.getMessage());
            return Mono.error(new RuntimeException("FIRESTORE_COMMENT_COUNT_UPDATE_FAILED"));
        }).then(); // Convertir el resultado a Mono<Void>
    }

}
