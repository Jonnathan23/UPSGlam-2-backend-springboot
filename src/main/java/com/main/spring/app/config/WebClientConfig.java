package com.main.spring.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${fastapi.url}")
    private String fastApiUrl;

    @Value("${firebase.api.key}")
    private String firebaseApiKey;

    private static final String FIREBASE_AUTH_BASE_URL = "https://identitytoolkit.googleapis.com/v1";

    @Bean(name = "firebaseAuthWebClient")
    public WebClient firebaseAuthWebClient(WebClient.Builder builder) {
        // Configurar timeout para evitar esperas indefinidas
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(10)); // Timeout de respuesta de 10 segundos

        return builder
                .baseUrl(FIREBASE_AUTH_BASE_URL)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    // 2. WebClient para el Servicio de FastAPI
    @Bean(name = "fastApiWebClient")
    public WebClient fastApiWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(fastApiUrl) // URL base: localhost:8000
                // Configuramos el buffer grande para la subida o descarga de imágenes
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }
}
