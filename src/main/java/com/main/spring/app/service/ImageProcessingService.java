package com.main.spring.app.service;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ImageProcessingService {

    private final WebClient webClient;

    public ImageProcessingService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<byte[]> processCanny(FilePart file, Integer kernelSize, Float sigma, String lowThreshold,
            String highThreshold, Boolean useAuto) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.asyncPart("file", file.content(), DataBuffer.class).filename(file.filename());

        if (kernelSize != null)
            builder.part("kernel_size", kernelSize);
        if (sigma != null)
            builder.part("sigma", sigma);
        if (lowThreshold != null)
            builder.part("low_threshold", lowThreshold);
        if (highThreshold != null)
            builder.part("high_threshold", highThreshold);
        if (useAuto != null)
            builder.part("use_auto", useAuto);

        return webClient.post()
                .uri("/api/canny")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(byte[].class);
    }
}
