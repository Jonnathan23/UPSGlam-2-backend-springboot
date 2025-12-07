package com.main.spring.app.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import com.main.spring.app.service.ImageProcessingService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/process")
public class ImageProcessingController {

        private final ImageProcessingService imageProcessingService;
        private final com.main.spring.app.service.SupabaseStorageService supabaseStorageService;

        public ImageProcessingController(ImageProcessingService imageProcessingService,
                        com.main.spring.app.service.SupabaseStorageService supabaseStorageService) {
                this.imageProcessingService = imageProcessingService;
                this.supabaseStorageService = supabaseStorageService;
        }

        @PostMapping(value = "/canny", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public Mono<ResponseEntity<com.main.spring.app.dto.ImageProcessResponse>> processCanny(
                        @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.Authentication authentication,
                        @RequestPart("file") FilePart file,
                        @RequestPart(value = "kernel_size", required = false) String kernelSize,
                        @RequestPart(value = "sigma", required = false) String sigma,
                        @RequestPart(value = "low_threshold", required = false) String lowThreshold,
                        @RequestPart(value = "high_threshold", required = false) String highThreshold,
                        @RequestPart(value = "use_auto", required = false) String useAuto) {

                String userName = authentication != null && authentication.getCredentials() != null
                                ? authentication.getCredentials().toString()
                                : "Unknown";
                Integer kSize = kernelSize != null ? Integer.parseInt(kernelSize) : 5;
                Float s = sigma != null ? Float.parseFloat(sigma) : 1.4f;
                Boolean auto = useAuto != null ? Boolean.parseBoolean(useAuto) : false;

                return imageProcessingService.processCanny(file, kSize, s, lowThreshold, highThreshold, auto)
                                .flatMap(bytes -> supabaseStorageService.uploadImage(bytes, file.filename()))
                                .map(url -> ResponseEntity
                                                .ok(new com.main.spring.app.dto.ImageProcessResponse(userName, url)));
        }

        @PostMapping(value = "/gaussian", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public Mono<ResponseEntity<com.main.spring.app.dto.ImageProcessResponse>> processGaussian(
                        @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.Authentication authentication,
                        @RequestPart("file") FilePart file,
                        @RequestPart(value = "kernel_size", required = false) String kernelSize,
                        @RequestPart(value = "sigma", required = false) String sigma,
                        @RequestPart(value = "use_auto", required = false) String useAuto) {

                String userName = authentication != null && authentication.getCredentials() != null
                                ? authentication.getCredentials().toString()
                                : "Unknown";
                Integer kSize = kernelSize != null ? Integer.parseInt(kernelSize) : 5;
                Float s = sigma != null ? Float.parseFloat(sigma) : 1.4f;
                Boolean auto = useAuto != null ? Boolean.parseBoolean(useAuto) : false;

                return imageProcessingService.processGaussian(file, kSize, s, auto)
                                .flatMap(bytes -> supabaseStorageService.uploadImage(bytes, file.filename()))
                                .map(url -> ResponseEntity
                                                .ok(new com.main.spring.app.dto.ImageProcessResponse(userName, url)));
        }

        @PostMapping(value = "/negative", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public Mono<ResponseEntity<com.main.spring.app.dto.ImageProcessResponse>> processNegative(
                        @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.Authentication authentication,
                        @RequestPart("file") FilePart file) {

                String userName = authentication != null && authentication.getCredentials() != null
                                ? authentication.getCredentials().toString()
                                : "Unknown";

                return imageProcessingService.processNegative(file)
                                .flatMap(bytes -> supabaseStorageService.uploadImage(bytes, file.filename()))
                                .map(url -> ResponseEntity
                                                .ok(new com.main.spring.app.dto.ImageProcessResponse(userName, url)));
        }

        @PostMapping(value = "/emboss", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public Mono<ResponseEntity<com.main.spring.app.dto.ImageProcessResponse>> processEmboss(
                        @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.Authentication authentication,
                        @RequestPart("file") FilePart file,
                        @RequestPart(value = "kernel_size", required = false) String kernelSize,
                        @RequestPart(value = "bias_value", required = false) String biasValue,
                        @RequestPart(value = "use_auto", required = false) String useAuto) {

                String userName = authentication != null && authentication.getCredentials() != null
                                ? authentication.getCredentials().toString()
                                : "Unknown";
                Integer kSize = kernelSize != null ? Integer.parseInt(kernelSize) : 3;
                Integer bias = biasValue != null ? Integer.parseInt(biasValue) : 128;
                Boolean auto = useAuto != null ? Boolean.parseBoolean(useAuto) : false;

                return imageProcessingService.processEmboss(file, kSize, bias, auto)
                                .flatMap(bytes -> supabaseStorageService.uploadImage(bytes, file.filename()))
                                .map(url -> ResponseEntity
                                                .ok(new com.main.spring.app.dto.ImageProcessResponse(userName, url)));
        }

        @PostMapping(value = "/watermark", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public Mono<ResponseEntity<com.main.spring.app.dto.ImageProcessResponse>> processWatermark(
                        @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.Authentication authentication,
                        @RequestPart("file") FilePart file,
                        @RequestPart(value = "scale", required = false) String scale,
                        @RequestPart(value = "transparency", required = false) String transparency,
                        @RequestPart(value = "spacing", required = false) String spacing) {

                String userName = authentication != null && authentication.getCredentials() != null
                                ? authentication.getCredentials().toString()
                                : "Unknown";
                Float sc = scale != null ? Float.parseFloat(scale) : 0.3f;
                Float tr = transparency != null ? Float.parseFloat(transparency) : 0.3f;
                Float sp = spacing != null ? Float.parseFloat(spacing) : 0.5f;

                return imageProcessingService.processWatermark(file, sc, tr, sp)
                                .flatMap(bytes -> supabaseStorageService.uploadImage(bytes, file.filename()))
                                .map(url -> ResponseEntity
                                                .ok(new com.main.spring.app.dto.ImageProcessResponse(userName, url)));
        }

        @PostMapping(value = "/ripple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public Mono<ResponseEntity<com.main.spring.app.dto.ImageProcessResponse>> processRipple(
                        @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.Authentication authentication,
                        @RequestPart("file") FilePart file,
                        @RequestPart(value = "edge_threshold", required = false) String edgeThreshold,
                        @RequestPart(value = "color_levels", required = false) String colorLevels,
                        @RequestPart(value = "saturation", required = false) String saturation) {

                String userName = authentication != null && authentication.getCredentials() != null
                                ? authentication.getCredentials().toString()
                                : "Unknown";
                Float edge = edgeThreshold != null ? Float.parseFloat(edgeThreshold) : 100.0f;
                Integer levels = colorLevels != null ? Integer.parseInt(colorLevels) : 8;
                Float sat = saturation != null ? Float.parseFloat(saturation) : 1.2f;

                return imageProcessingService.processRipple(file, edge, levels, sat)
                                .flatMap(bytes -> supabaseStorageService.uploadImage(bytes, file.filename()))
                                .map(url -> ResponseEntity
                                                .ok(new com.main.spring.app.dto.ImageProcessResponse(userName, url)));
        }

        @PostMapping(value = "/collage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public Mono<ResponseEntity<com.main.spring.app.dto.ImageProcessResponse>> processCollage(
                        @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.Authentication authentication,
                        @RequestPart("file") FilePart file) {

                String userName = authentication != null && authentication.getCredentials() != null
                                ? authentication.getCredentials().toString()
                                : "Unknown";

                return imageProcessingService.processCollage(file)
                                .flatMap(bytes -> supabaseStorageService.uploadImage(bytes, file.filename()))
                                .map(url -> ResponseEntity
                                                .ok(new com.main.spring.app.dto.ImageProcessResponse(userName, url)));
        }
}
