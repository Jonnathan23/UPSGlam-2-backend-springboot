package com.main.spring.app.controller;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.main.spring.app.interfaces.posts.PostService;
import com.main.spring.app.schema.PostsSchema;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/posts")
public class PostsControllerImpl {

    private final PostService postService;

    public PostsControllerImpl(PostService postService) {
        this.postService = postService;
    }

    @PostMapping(consumes = { "multipart/form-data" }) // HABILITA FormData
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> createPost(
            @RequestPart("pos_image") Mono<FilePart> filePartMono, // Archivo
            @RequestPart("pos_caption") String caption, // Texto
            Authentication authentication // JWT validado
    ) {
        // 1. EXTRAER UID del JWT (que Spring Security ya verificó)
        // El principal es el UID que devolvimos en el FirebaseAuthenticationManager
        String authorUid = (String) authentication.getPrincipal();

        // 2. VALIDACIÓN MANUAL de los datos (ya que no podemos usar @Valid en FilePart)
        if (caption == null || caption.trim().isEmpty()) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La caption no puede estar vacía."));
        }

        // 3. DELEGAR al servicio: Pasamos el Mono<FilePart>, caption y UID
        return filePartMono
                .flatMap(filePart -> this.postService.createPost(filePart, caption, authorUid))
                .thenReturn("Publicación creada correctamente"); // Retorna mensaje de éxito
    }

    @GetMapping("/by-author/{authorUid}") // 👈 Endpoint GET con Path Variable
    public Flux<PostsSchema> getPostsByAuthor(@PathVariable String authorUid) {
        
        // El Controller solo delega la consulta al Service
        return postService.getPostsByAuthor(authorUid);
    }

    @DeleteMapping("/{postId}") // 👈 Endpoint DELETE con Path Variable
    @ResponseStatus(HttpStatus.OK)
    public Mono<String> deletePost(
            @PathVariable String postId,
            Authentication authentication // JWT validado
    ) {
        // 1. EXTRAER UID del JWT (que Spring Security ya verificó)
        String authorUid = (String) authentication.getPrincipal();

        // 2. DELEGAR al servicio: Pasamos el postId y el UID para validación
        return postService.deletePost(postId, authorUid);
    }
}
