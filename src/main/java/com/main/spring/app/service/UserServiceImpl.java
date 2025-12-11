package com.main.spring.app.service;

import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.main.spring.app.dto.UserSearchResponse;
import com.main.spring.app.interfaces.users.UserRepository;
import com.main.spring.app.interfaces.users.UserService;

import org.springframework.http.HttpStatus;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserServiceImpl implements UserService {

        private final UserRepository userRepository;
        private final SupabaseStorageService supabaseStorageService;
        private static final int SEARCH_LIMIT = 5;

        public UserServiceImpl(UserRepository userRepository, SupabaseStorageService supabaseStorageService) {
                this.userRepository = userRepository;
                this.supabaseStorageService = supabaseStorageService;
        }

        @Override
        public Flux<UserSearchResponse> searchUsers(String query) {
                if (query == null || query.trim().isEmpty()) {
                        return Flux.empty();
                }

                return userRepository.searchUsers(query.trim(), SEARCH_LIMIT)
                                .onErrorResume(e -> {
                                        System.err.println("ERROR: Fallo al buscar usuarios. Causa: " + e.getMessage());
                                        return Flux.empty();
                                });
        }

        @Override
        public Mono<String> updatePhoto(String userId, FilePart filePart) {
                // 1. Verificar que el usuario existe
                return userRepository.getUserById(userId)
                                .switchIfEmpty(Mono.error(new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "El usuario no existe.")))
                                .flatMap(user -> {
                                        // 2. Convertir FilePart a byte[] de forma reactiva
                                        Mono<byte[]> imageBytesMono = DataBufferUtils.join(filePart.content())
                                                        .map(dataBuffer -> {
                                                                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                                                dataBuffer.read(bytes);
                                                                DataBufferUtils.release(dataBuffer);
                                                                return bytes;
                                                        });

                                        // 3. Subir la imagen al bucket y obtener la URL
                                        Mono<String> imageUrlMono = imageBytesMono
                                                        .flatMap(bytes -> supabaseStorageService.uploadImage(bytes,
                                                                        filePart.filename()));

                                        // 4. Actualizar el photoUrl en Firestore
                                        return imageUrlMono
                                                        .flatMap(photoUrl -> userRepository.updatePhotoUrl(userId,
                                                                        photoUrl))
                                                        .thenReturn("Foto de perfil actualizada correctamente")
                                                        .doOnSuccess(message -> System.out.println(
                                                                        "LOG: Foto de perfil actualizada para usuario: "
                                                                                        + userId))
                                                        .onErrorResume(e -> {
                                                                System.err.println(
                                                                                "ERROR: Fallo al actualizar foto de perfil. Causa: "
                                                                                                + e.getMessage());
                                                                return Mono.error(new ResponseStatusException(
                                                                                HttpStatus.INTERNAL_SERVER_ERROR,
                                                                                "Error interno al actualizar la foto de perfil."));
                                                        });
                                });
        }

        @Override
        public Mono<String> updateBio(String userId, String bio) {
                // 1. Validar que la bio no esté vacía
                if (bio == null || bio.trim().isEmpty()) {
                        return Mono.error(new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST, "La biografía no puede estar vacía."));
                }

                // 2. Verificar que el usuario existe
                return userRepository.getUserById(userId)
                                .switchIfEmpty(Mono.error(new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "El usuario no existe.")))
                                .flatMap(user -> {
                                        // 3. Actualizar la bio en Firestore
                                        return userRepository.updateBio(userId, bio.trim())
                                                        .thenReturn("Biografía actualizada correctamente")
                                                        .doOnSuccess(message -> System.out.println(
                                                                        "LOG: Biografía actualizada para usuario: "
                                                                                        + userId))
                                                        .onErrorResume(e -> {
                                                                System.err.println(
                                                                                "ERROR: Fallo al actualizar biografía. Causa: "
                                                                                                + e.getMessage());
                                                                return Mono.error(new ResponseStatusException(
                                                                                HttpStatus.INTERNAL_SERVER_ERROR,
                                                                                "Error interno al actualizar la biografía."));
                                                        });
                                });
        }
}
