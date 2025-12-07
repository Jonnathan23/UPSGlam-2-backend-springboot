package com.main.spring.app.security;

import com.google.firebase.auth.FirebaseAuth;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Component
public class FirebaseAuthenticationManager implements ReactiveAuthenticationManager {

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();

        return Mono.fromCallable(() -> {
            try {
                return FirebaseAuth.getInstance().verifyIdToken(authToken);
            } catch (Exception e) {
                throw new org.springframework.security.authentication.BadCredentialsException("Invalid Firebase Token",
                        e);
            }
        })
                .map(decodedToken -> {
                    String uid = decodedToken.getUid();
                    // Aquí podrías extraer roles si los tienes en el token
                    List<SimpleGrantedAuthority> authorities = Collections
                            .singletonList(new SimpleGrantedAuthority("ROLE_USER"));
                    return new UsernamePasswordAuthenticationToken(uid, null, authorities);
                });
    }
}
