package com.main.spring.app.service;

import org.springframework.stereotype.Service;

import com.main.spring.app.interfaces.comments.CommentsRepository;
import com.main.spring.app.interfaces.comments.CommentsService;
import com.main.spring.app.interfaces.posts.PostRepository;

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

}
