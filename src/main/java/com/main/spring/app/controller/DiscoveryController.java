package com.main.spring.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Controlador simple para descubrimiento de servidores.
 * Permite que las aplicaciones cliente detecten este servidor Spring Boot en la red local.
 * 
 * Soporta tanto GET como HEAD requests automáticamente.
 */
@RestController
@RequestMapping("/discovery")
public class DiscoveryController {

    /**
     * Endpoint de descubrimiento simple y rápido.
     * Retorna un mensaje simple para identificar que este es el servidor correcto.
     * 
     * Spring WebFlux maneja automáticamente HEAD requests para este endpoint.
     * 
     * @return Mono con respuesta simple "UPSGlam-Server"
     */
    @GetMapping
    public Mono<ResponseEntity<String>> discover() {
        return Mono.just(ResponseEntity.ok("UPSGlam-Server"));
    }
}

