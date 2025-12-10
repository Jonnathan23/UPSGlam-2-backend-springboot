package com.main.spring.app.service;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.main.spring.app.interfaces.comments.CommentsRepository;
import com.main.spring.app.interfaces.comments.CommentsService;
import com.main.spring.app.interfaces.posts.PostRepository;

import org.springframework.http.HttpStatus;

import reactor.core.publisher.Mono;

@Service
public class CommentsServiceImpl implements CommentsService {

    private final CommentsRepository commentsRepository;
    private final PostRepository postRepository;

    public CommentsServiceImpl(CommentsRepository commentsRepository, PostRepository postRepository) {
        this.commentsRepository = commentsRepository;
        this.postRepository = postRepository;
    }

    @Override
    public Mono<String> createComment(String postId, String authorUid, String text) {

        return commentsRepository.createComment(postId, authorUid, text) // 1. CREAR EL COMENTARIO
                .flatMap(commentId -> {

                    // Si el comentario fue creado exitosamente, procedemos a actualizar el
                    // contador.
                    // 2. ACTUALIZAR EL CONTADOR (+1)
                    return postRepository.updateCommentCount(postId, 1)
                            .thenReturn("Comentario creado exitosamente con ID: " + commentId); // Devolvemos el mensaje
                                                                                                // final
                })
                .doOnSuccess(message -> System.out
                        .println("LOG: Comentario y contador actualizados exitosamente. Mensaje: " + message))
                .onErrorResume(e ->
                // Propagamos el error de fallo de la base de datos
                Mono.error(e));
    }

    @Override
    public Mono<String> deleteComment(String postId, String commentId, String authorUid) {
        // 1. Obtener el comentario para validar autoría
        return commentsRepository.getCommentById(postId, commentId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El comentario no existe.")))
                .flatMap(comment -> {
                    // 2. Validar que el autor del comentario coincida con el UID del JWT
                    if (!comment.getCom_authorUid().equals(authorUid)) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.FORBIDDEN, "No tienes permiso para eliminar este comentario."));
                    }

                    // 3. Eliminar el comentario de Firestore
                    Mono<Void> deleteCommentMono = commentsRepository.deleteComment(postId, commentId);

                    // 4. Actualizar el contador de comentarios del post (-1)
                    Mono<Void> updateCountMono = postRepository.updateCommentCount(postId, -1);

                    // 5. Ejecutar ambas operaciones y retornar mensaje de éxito
                    return deleteCommentMono
                            .then(updateCountMono)
                            .thenReturn("Comentario eliminado correctamente")
                            .doOnSuccess(message -> System.out
                                    .println("LOG: Comentario eliminado exitosamente con ID: " + commentId))
                            .onErrorResume(e -> {
                                System.err.println("ERROR: Fallo al eliminar el comentario. Causa: " + e.getMessage());
                                return Mono.error(new ResponseStatusException(
                                        HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al eliminar el comentario."));
                            });
                });
    }

}
