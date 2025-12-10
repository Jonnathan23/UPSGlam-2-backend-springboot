package com.main.spring.app.service;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.main.spring.app.interfaces.posts.PostRepository;
import com.main.spring.app.interfaces.posts.PostService;
import com.main.spring.app.schema.PostsSchema;
import com.main.spring.app.service.SupabaseStorageService;

import org.springframework.http.HttpStatus;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PostsServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final SupabaseStorageService supabaseStorageService;

    public PostsServiceImpl(PostRepository postRepository, SupabaseStorageService supabaseStorageService) {
        this.postRepository = postRepository;
        this.supabaseStorageService = supabaseStorageService;
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

    @Override
    public Flux<PostsSchema> getPostsByAuthor(String authorUid) {

        // Delegamos la consulta directamente al repositorio
        return postRepository.getPostsByAuthor(authorUid)
                .onErrorResume(e -> {
                    System.err.println("ERROR: Fallo al consultar posts por autor. Causa: " + e.getMessage());
                    // Devolvemos un Flux vacío en caso de error de consulta
                    return Flux.empty();
                });
    }

    @Override
    public Mono<String> deletePost(String postId, String authorUid) {
        // 1. Obtener el post para validar autoría y obtener la URL de la imagen
        return postRepository.getPostById(postId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El post no existe.")))
                .flatMap(post -> {
                    // 2. Validar que el autor del post coincida con el UID del JWT
                    if (!post.getPos_authorUid().equals(authorUid)) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.FORBIDDEN, "No tienes permiso para eliminar este post."));
                    }

                    // 3. Eliminar la imagen de Supabase Storage
                    Mono<Void> deleteImageMono = supabaseStorageService.deleteImage(post.getPos_imageUrl())
                            .onErrorResume(e -> {
                                // Si falla la eliminación de la imagen, logueamos pero continuamos
                                System.err.println("ADVERTENCIA: No se pudo eliminar la imagen de Supabase: " + e.getMessage());
                                return Mono.empty(); // Continuamos con la eliminación del post
                            });

                    // 4. Eliminar el post de Firestore
                    Mono<Void> deletePostMono = postRepository.deletePost(postId);

                    // 5. Ejecutar ambas operaciones y retornar mensaje de éxito
                    return deleteImageMono
                            .then(deletePostMono)
                            .thenReturn("Post eliminado correctamente")
                            .doOnSuccess(message -> System.out.println("LOG: Post eliminado exitosamente con ID: " + postId))
                            .onErrorResume(e -> {
                                System.err.println("ERROR: Fallo al eliminar el post. Causa: " + e.getMessage());
                                return Mono.error(new ResponseStatusException(
                                        HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al eliminar el post."));
                            });
                });
    }
}
