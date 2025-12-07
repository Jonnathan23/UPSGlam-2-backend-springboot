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
                    String name = decodedToken.getName(); // Extract name
                    // Aquí podrías extraer roles si los tienes en el token
                    List<SimpleGrantedAuthority> authorities = Collections
                            .singletonList(new SimpleGrantedAuthority("ROLE_USER"));
                    // Use a custom principal or just pass the name as the principal if that's what
                    // we want,
                    // but usually principal is the ID. Let's pass a custom object or just the name
                    // if the ID isn't strictly needed there,
                    // OR better: keep UID as principal and add name to details?
                    // Actually, let's create a simple User object or just pass the name if the user
                    // only cares about the name.
                    // For now, let's pass a Map or custom object as the principal? No, Principal
                    // should be the ID usually.
                    // Let's pass the UID as principal, and we can't easily set details in reactive
                    // auth manager this way without a custom token class.
                    // Simplest approach: Pass a custom UserDetails-like object as the principal.

                    // Let's just pass the name as the credentials or use a custom Authentication
                    // implementation?
                    // Standard: UsernamePasswordAuthenticationToken(principal, credentials,
                    // authorities)
                    // Principal = UID
                    // Credentials = Name (hacky but works for this specific requirement)
                    return new UsernamePasswordAuthenticationToken(uid, name, authorities);
                });
    }
}
