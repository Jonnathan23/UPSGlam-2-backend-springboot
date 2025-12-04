package com.main.spring.app.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.main.spring.app.interfaces.auth.AuthRepository;
import com.main.spring.app.model.RegisterRequest;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class AuthRepositoryImpl implements AuthRepository {

    private final FirebaseAuth firebaseAuth;

    public AuthRepositoryImpl(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }  
   

    @Override
    public Mono<String> registerUser(RegisterRequest request) {
        
        return Mono.fromCallable(() -> {
            UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                    .setEmail(request.getEmail())
                    .setPassword(request.getPassword())
                    .setDisplayName(request.getEmail().split("@")[0]); // Usar parte del email como nombre

            UserRecord userRecord = firebaseAuth.createUser(createRequest);

            // Retorna la UID de Firebase como el identificador de la cuenta
            return userRecord.getUid();
        });
    }

}
