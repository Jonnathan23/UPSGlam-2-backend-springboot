package com.main.spring.app.service;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.main.spring.app.interfaces.posts.PostRepository;
import com.main.spring.app.interfaces.posts.PostService;

import org.springframework.http.HttpStatus;

import reactor.core.publisher.Mono;

@Service
public class PostsServiceImpl implements PostService {

    private final PostRepository postRepository;

    public PostsServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public Mono<String> createPost(FilePart filePart, String caption, String authorUid) {

        return postRepository.createPost(filePart, caption, authorUid)
                .doOnSuccess(postId ->
                // Log de éxito: Indica que la publicación y la subida fueron completadas.
                System.out.println("LOG: Post creado exitosamente con ID: " + postId))
                .onErrorResume(e -> {
                    // Manejo de Errores: Capturamos cualquier excepción lanzada por el repositorio
                    // (subida/Firestore).
                    String errorMessage = e.getMessage();

                    // Si el repositorio lanza un error de Runtime (ej: fallo de conexión, permisos)
                    if (errorMessage != null && errorMessage.contains("STORAGE_UPLOAD_FAILED")) {
                        System.err.println("ERROR: Fallo al subir la imagen al Bucket. Causa: " + errorMessage);
                        // Lanzamos un 500 para el cliente, ocultando el detalle técnico.
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR, "Fallo al procesar la imagen de la publicación."));
                    }

                    // Errores de Firestore (ej: Bad Request si los datos están mal)
                    if (errorMessage != null && errorMessage.contains("FIRESTORE_SAVE_FAILED")) {
                        System.err.println("ERROR: Fallo al guardar metadata en Firestore. Causa: " + errorMessage);
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR, "Fallo al guardar la metadata de la publicación."));
                    }

                    // Fallback para cualquier otro error
                    System.err.println("ERROR: Error no manejado durante la creación del Post. Causa: " + errorMessage);
                    return Mono.error(new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al crear el post."));
                });
    }
}
