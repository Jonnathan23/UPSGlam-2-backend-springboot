package com.main.spring.app.config;

import com.main.spring.app.security.BearerTokenServerAuthenticationConverter;
import com.main.spring.app.security.FirebaseAuthenticationManager;
import com.main.spring.app.security.AuthenticationFailureHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

        @Bean
        public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                        FirebaseAuthenticationManager authManager,
                        BearerTokenServerAuthenticationConverter authConverter,
                        AuthenticationFailureHandler authenticationFailureHandler) {

                AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(authManager);
                authenticationWebFilter.setServerAuthenticationConverter(authConverter);
                authenticationWebFilter.setAuthenticationFailureHandler(
                                new ServerAuthenticationEntryPointFailureHandler(authenticationFailureHandler));

                return http
                                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint(authenticationFailureHandler))
                                .authorizeExchange(exchanges -> exchanges
                                                .pathMatchers("/api/auth/register", "/api/test/**", "/api/auth/login")
                                                .permitAll()
                                                .pathMatchers("/actuator/health", "/discovery").permitAll() // Permitir
                                                                                                            // health
                                                                                                            // check y
                                                                                                            // discovery
                                                                                                            // sin
                                                                                                            // autenticación
                                                .anyExchange().authenticated())
                                .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                                .build();
        }

}
