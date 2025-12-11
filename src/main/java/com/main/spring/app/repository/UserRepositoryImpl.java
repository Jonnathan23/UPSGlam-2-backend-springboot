package com.main.spring.app.repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.main.spring.app.dto.UserSearchResponse;
import com.main.spring.app.interfaces.users.UserRepository;
import com.main.spring.app.schema.UserSchema;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final Firestore firestoreDb;

    public UserRepositoryImpl(Firestore firestoreDb) {
        this.firestoreDb = firestoreDb;
    }

    @Override
    public Flux<UserSearchResponse> searchUsers(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return Flux.empty();
        }

        String searchQuery = query.trim();
        String searchQueryLower = searchQuery.toLowerCase();
        String searchQueryEnd = searchQuery + "\uf8ff"; // Carácter Unicode para búsqueda de prefijo

        return Mono.fromCallable(() -> {
            Set<UserSearchResponse> results = new HashSet<>();

            // 1. Búsqueda por username (prefijo) - case-sensitive para Firestore
            QuerySnapshot usernameSnapshot = firestoreDb.collection("Users")
                    .whereGreaterThanOrEqualTo("usr_username", searchQuery)
                    .whereLessThan("usr_username", searchQueryEnd)
                    .limit(limit * 2) // Obtener más para filtrar después
                    .get()
                    .get();

            usernameSnapshot.getDocuments().forEach(document -> {
                UserSchema user = document.toObject(UserSchema.class);
                if (user != null) {
                    String username = user.getUsr_username() != null ? user.getUsr_username() : "";
                    String email = user.getUsr_email() != null ? user.getUsr_email() : "";
                    // Filtrar en memoria para búsqueda case-insensitive y tipo LIKE
                    if (username.toLowerCase().contains(searchQueryLower) ||
                            email.toLowerCase().contains(searchQueryLower)) {
                        results.add(new UserSearchResponse(
                                document.getId(),
                                username,
                                email,
                                user.getUsr_photoUrl()));
                    }
                }
            });

            // 2. Búsqueda por email (prefijo) - case-sensitive para Firestore
            QuerySnapshot emailSnapshot = firestoreDb.collection("Users")
                    .whereGreaterThanOrEqualTo("usr_email", searchQuery)
                    .whereLessThan("usr_email", searchQueryEnd)
                    .limit(limit * 2) // Obtener más para filtrar después
                    .get()
                    .get();

            emailSnapshot.getDocuments().forEach(document -> {
                UserSchema user = document.toObject(UserSchema.class);
                if (user != null) {
                    String username = user.getUsr_username() != null ? user.getUsr_username() : "";
                    String email = user.getUsr_email() != null ? user.getUsr_email() : "";
                    // Filtrar en memoria para búsqueda case-insensitive y tipo LIKE
                    if (username.toLowerCase().contains(searchQueryLower) ||
                            email.toLowerCase().contains(searchQueryLower)) {
                        results.add(new UserSearchResponse(
                                document.getId(),
                                username,
                                email,
                                user.getUsr_photoUrl()));
                    }
                }
            });

            // 3. Filtrar y limitar resultados finales
            List<UserSearchResponse> finalResults = results.stream()
                    .filter(user -> {
                        String username = user.getUsername() != null ? user.getUsername().toLowerCase() : "";
                        String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";
                        return username.contains(searchQueryLower) || email.contains(searchQueryLower);
                    })
                    .limit(limit)
                    .collect(Collectors.toList());

            return finalResults;
        }).onErrorMap(e -> {
            System.err.println("Error de Firestore al buscar usuarios: " + e.getMessage());
            return new RuntimeException("FIRESTORE_SEARCH_USERS_FAILED", e);
        }).flatMapIterable(results -> results);
    }

    @Override
    public Mono<UserSchema> getUserById(String userId) {
        return Mono.fromCallable(() -> {
            var documentSnapshot = firestoreDb.collection("Users")
                    .document(Objects.requireNonNull(userId, "userId no puede ser null"))
                    .get()
                    .get();

            if (!documentSnapshot.exists()) {
                return null;
            }

            return documentSnapshot.toObject(UserSchema.class);
        }).onErrorMap(e -> {
            System.err.println("Error de Firestore al obtener usuario por ID: " + e.getMessage());
            return new RuntimeException("FIRESTORE_GET_USER_FAILED", e);
        });
    }

    @Override
    public Mono<Void> updatePhotoUrl(String userId, String photoUrl) {
        return Mono.fromCallable(() -> {
            DocumentReference userRef = firestoreDb.collection("Users")
                    .document(Objects.requireNonNull(userId, "userId no puede ser null"));

            Map<String, Object> update = Collections.singletonMap(
                    "usr_photoUrl",
                    Objects.requireNonNull(photoUrl, "photoUrl no puede ser null"));

            userRef.update(update).get();
            return null;
        }).onErrorMap(e -> {
            System.err.println("Error de Firestore al actualizar photoUrl: " + e.getMessage());
            return new RuntimeException("FIRESTORE_UPDATE_PHOTO_FAILED", e);
        }).then();
    }

    @Override
    public Mono<Void> updateBio(String userId, String bio) {
        return Mono.fromCallable(() -> {
            DocumentReference userRef = firestoreDb.collection("Users")
                    .document(Objects.requireNonNull(userId, "userId no puede ser null"));

            Map<String, Object> update = Collections.singletonMap(
                    "usr_bio",
                    Objects.requireNonNull(bio, "bio no puede ser null"));

            userRef.update(update).get();
            return null;
        }).onErrorMap(e -> {
            System.err.println("Error de Firestore al actualizar bio: " + e.getMessage());
            return new RuntimeException("FIRESTORE_UPDATE_BIO_FAILED", e);
        }).then();
    }

    @Override
    public Mono<UserSchema> findUserByEmail(String email) {
        return Mono.fromCallable(() -> {
            QuerySnapshot snapshot = firestoreDb.collection("Users")
                    .whereEqualTo("usr_email", email)
                    .limit(1)
                    .get()
                    .get();

            if (snapshot.isEmpty()) {
                return null;
            }

            var document = snapshot.getDocuments().get(0);
            UserSchema user = document.toObject(UserSchema.class);
            if (user != null) {
                user.setUsr_id(document.getId());
            }
            return user;
        }).onErrorMap(e -> {
            System.err.println("ERROR: Fallo al buscar usuario por email " + email + ". Causa: " + e.getMessage());
            return new RuntimeException("FIRESTORE_FIND_BY_EMAIL_FAILED", e);
        });
    }
}
