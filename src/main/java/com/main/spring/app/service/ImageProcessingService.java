package com.main.spring.app.service;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Qualifier;
import reactor.core.publisher.Mono;

@Service
public class ImageProcessingService {

    private final WebClient webClient;

    public ImageProcessingService(
            @Qualifier("fastApiWebClient") WebClient webClient) {
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

    public Mono<byte[]> processGaussian(FilePart file, Integer kernelSize, Float sigma, Boolean useAuto) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.asyncPart("file", file.content(), DataBuffer.class).filename(file.filename());

        if (kernelSize != null)
            builder.part("kernel_size", kernelSize);
        if (sigma != null)
            builder.part("sigma", sigma);
        if (useAuto != null)
            builder.part("use_auto", useAuto);

        return webClient.post()
                .uri("/api/gaussian")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(byte[].class);
    }

    public Mono<byte[]> processNegative(FilePart file) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.asyncPart("file", file.content(), DataBuffer.class).filename(file.filename());

        return webClient.post()
                .uri("/api/negative")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(byte[].class);
    }

    public Mono<byte[]> processEmboss(FilePart file, Integer kernelSize, Integer biasValue, Boolean useAuto) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.asyncPart("file", file.content(), DataBuffer.class).filename(file.filename());

        if (kernelSize != null)
            builder.part("kernel_size", kernelSize);
        if (biasValue != null)
            builder.part("bias_value", biasValue);
        if (useAuto != null)
            builder.part("use_auto", useAuto);

        return webClient.post()
                .uri("/api/emboss")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(byte[].class);
    }

    public Mono<byte[]> processWatermark(FilePart file, Float scale, Float transparency, Float spacing) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.asyncPart("file", file.content(), DataBuffer.class).filename(file.filename());

        if (scale != null)
            builder.part("scale", scale);
        if (transparency != null)
            builder.part("transparency", transparency);
        if (spacing != null)
            builder.part("spacing", spacing);

        return webClient.post()
                .uri("/api/watermark")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(byte[].class);
    }

    public Mono<byte[]> processRipple(FilePart file, Float edgeThreshold, Integer colorLevels, Float saturation) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.asyncPart("file", file.content(), DataBuffer.class).filename(file.filename());

        if (edgeThreshold != null)
            builder.part("edge_threshold", edgeThreshold);
        if (colorLevels != null)
            builder.part("color_levels", colorLevels);
        if (saturation != null)
            builder.part("saturation", saturation);

        return webClient.post()
                .uri("/api/ripple")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(byte[].class);
    }

    public Mono<byte[]> processCollage(FilePart file) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.asyncPart("file", file.content(), DataBuffer.class).filename(file.filename());

        return webClient.post()
                .uri("/api/collage")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(byte[].class);
    }
}
