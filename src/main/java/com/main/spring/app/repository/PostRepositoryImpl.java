package com.main.spring.app.repository;

import org.springframework.core.io.buffer.DataBufferUtils;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.codec.multipart.FilePart;
import com.google.cloud.firestore.QuerySnapshot;

import com.google.cloud.firestore.Firestore;
import com.main.spring.app.interfaces.posts.PostRepository;
import com.main.spring.app.schema.PostsSchema;
import com.main.spring.app.service.SupabaseStorageService;

import com.google.cloud.firestore.DocumentReference; // Necesario
import com.google.cloud.firestore.FieldValue;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Repository;
import java.util.Objects;
import java.util.List;
import java.nio.ByteBuffer;

@Repository
public class PostRepositoryImpl implements PostRepository {

    private final Firestore firestoreDb;
    private final SupabaseStorageService supabaseStorageService;

    public PostRepositoryImpl(Firestore firestoreDb, SupabaseStorageService supabaseStorageService) {
        this.firestoreDb = firestoreDb;
        this.supabaseStorageService = supabaseStorageService;
    }

    @Override
    public Mono<String> createPost(FilePart filePart, String caption, String authorUid) {

        // 1. Convertir FilePart a byte[] de forma reactiva
        Mono<byte[]> imageBytesMono = DataBufferUtils.join(filePart.content())
                .map(dataBuffer -> {
                    ByteBuffer byteBuffer = dataBuffer.asByteBuffer();
                    byte[] bytes = new byte[byteBuffer.remaining()];
                    byteBuffer.get(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return bytes;
                });

        // 2. Subir la imagen al bucket y obtener la URL
        Mono<String> imageUrlMono = imageBytesMono
                .flatMap(bytes -> saveImageInBucket(bytes, filePart.filename()));

        // 3. Encadenar la URL obtenida para crear el PostSchema y guardarlo en
        // Firestore
        return imageUrlMono.flatMap(imageUrl -> Mono.fromCallable(() -> {

            // Usamos el constructor que creamos en PostsSchema
            PostsSchema newPost = new PostsSchema(
                    authorUid,
                    imageUrl, // URL obtenida del bucket
                    caption);

            // 4. Obtener la referencia a la colección 'posts'
            // NOTA: Firestore no necesita que incluyas timestamp en el constructor si
            // usamos Timestamp.now() en el Schema
            DocumentReference docRef = firestoreDb.collection("Posts").document();

            // 5. Guardar el objeto POJO (Bloqueante)
            // .set() guarda el objeto, y .get() hace que la operación sea síncrona
            docRef.set(newPost).get();

            // 6. Retornar el ID del documento recién creado
            return docRef.getId();

        }));
    }

    public Mono<String> saveImageInBucket(byte[] imageBytes, String filename) {
        return supabaseStorageService.uploadImage(imageBytes, filename);
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

    // *Gets */
    @Override
    public Flux<PostsSchema> getPostsByAuthor(String authorUid) {

        // 1. Ejecutamos la consulta bloqueante dentro de un Mono.fromCallable
        Mono<List<PostsSchema>> postsListMono = Mono.fromCallable(() -> {

            // Consulta Firestore: Colección "Posts" donde pos_authorUid sea igual al
            // parámetro
            QuerySnapshot snapshot = firestoreDb.collection("Posts")
                    .whereEqualTo("pos_authorUid", authorUid)
                    .orderBy("pos_timestamp", com.google.cloud.firestore.Query.Direction.DESCENDING) // Orden
                                                                                                     // cronológico
                    .get() // Ejecuta la llamada bloqueante
                    .get(); // Obtiene el resultado (lanza ExecutionException si falla)

            // Mapeamos los resultados:
            List<PostsSchema> posts = snapshot.getDocuments()
                    .stream()
                    .map(document -> document.toObject(PostsSchema.class))
                    .toList();

            return posts;

        }).onErrorMap(e -> {
            System.err.println("Error de Firestore al consultar: " + e.getMessage());
            return new RuntimeException("FIRESTORE_QUERY_FAILED", e);
        });

        // 2. Convertimos el Mono<List<PostsSchema>> a un Flux<PostsSchema>
        // Esto permite que el Controller reciba el stream reactivo de posts.
        return postsListMono.flatMapIterable(posts -> posts);
    }

}
