package com.main.spring.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class SupabaseStorageService {

    private final WebClient webClient;

    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Value("${supabase.key:}")
    private String supabaseKey;

    @Value("${supabase.bucket:}")
    private String supabaseBucket;

    public SupabaseStorageService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<String> uploadImage(byte[] imageBytes, String filename) {
        if (supabaseUrl.isEmpty() || supabaseKey.isEmpty() || supabaseBucket.isEmpty()) {
            return Mono.error(new RuntimeException("Supabase configuration is missing in application.properties"));
        }

        // Generar un nombre único y sanitizado
        // Asegurar que la extensión sea .png ya que el filtro devuelve PNG
        String safeFilename = filename.replaceAll("[^a-zA-Z0-9.-]", "_");
        if (!safeFilename.toLowerCase().endsWith(".png")) {
            safeFilename = safeFilename + ".png";
        }
        String uniqueFilename = UUID.randomUUID().toString() + "_" + safeFilename;

        // Construir la URL correcta para la API REST de Supabase Storage
        // ... (existing comments) ...

        // Hack para extraer el project ID si la URL es la de S3
        String projectId = "qfmfnvmuceggqkueiypy"; // Fallback default based on user input
        if (supabaseUrl.contains(".")) {
            try {
                String host = java.net.URI.create(supabaseUrl).getHost();
                projectId = host.split("\\.")[0];
            } catch (Exception e) {
                // Ignore
            }
        }

        String finalProjectId = projectId;
        String storageUrl = "https://" + finalProjectId + ".supabase.co/storage/v1/object/" + supabaseBucket + "/"
                + uniqueFilename;

        return webClient.post()
                .uri(storageUrl)
                .header("Authorization", "Bearer " + supabaseKey)
                .contentType(MediaType.IMAGE_PNG)
                .body(BodyInserters.fromValue(imageBytes))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("Error uploading to Supabase: "
                                        + clientResponse.statusCode() + " - " + errorBody))))
                .bodyToMono(String.class)
                .map(response -> getPublicUrl(finalProjectId, uniqueFilename));
    }

    private String getPublicUrl(String projectId, String filename) {
        return "https://" + projectId + ".supabase.co/storage/v1/object/public/" + supabaseBucket + "/" + filename;
    }
}
