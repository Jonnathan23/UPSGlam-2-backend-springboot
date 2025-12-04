package com.main.spring.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
                // 1. Deshabilitar la protección CSRF (necesario para APIs REST sin sesiones)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // 2. Definir las reglas de autorización de peticiones
                .authorizeExchange(exchanges -> exchanges
                        // Permitir acceso sin autenticación a la ruta de registro
                        .pathMatchers("/api/auth/register", "/api/test/**").permitAll()

                        // Permitir acceso sin autenticación a la ruta de login
                        .pathMatchers("/api/auth/login").permitAll()

                        // Proteger todas las demás rutas, requiriendo autenticación
                        .anyExchange().authenticated())
                // 3. Deshabilitar la ventana de login HTTP Basic por defecto
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .build();
    }

}
