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

        public ImageProcessingController(ImageProcessingService imageProcessingService) {
                this.imageProcessingService = imageProcessingService;
        }

        @PostMapping(value = "/canny", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.IMAGE_PNG_VALUE)
        public Mono<ResponseEntity<byte[]>> processCanny(
                        @RequestPart("file") FilePart file,
                        @RequestPart(value = "kernel_size", required = false) String kernelSize,
                        @RequestPart(value = "sigma", required = false) String sigma,
                        @RequestPart(value = "low_threshold", required = false) String lowThreshold,
                        @RequestPart(value = "high_threshold", required = false) String highThreshold,
                        @RequestPart(value = "use_auto", required = false) String useAuto) {

                Integer kSize = kernelSize != null ? Integer.parseInt(kernelSize) : 5;
                Float s = sigma != null ? Float.parseFloat(sigma) : 1.4f;
                Boolean auto = useAuto != null ? Boolean.parseBoolean(useAuto) : false;

                return imageProcessingService.processCanny(file, kSize, s, lowThreshold, highThreshold, auto)
                                .map(bytes -> ResponseEntity
                                                .ok()
                                                .contentType(MediaType.IMAGE_PNG)
                                                .body(bytes));
        }

        @PostMapping(value = "/gaussian", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.IMAGE_PNG_VALUE)
        public Mono<ResponseEntity<byte[]>> processGaussian(
                        @RequestPart("file") FilePart file,
                        @RequestPart(value = "kernel_size", required = false) String kernelSize,
                        @RequestPart(value = "sigma", required = false) String sigma,
                        @RequestPart(value = "use_auto", required = false) String useAuto) {

                Integer kSize = kernelSize != null ? Integer.parseInt(kernelSize) : 5;
                Float s = sigma != null ? Float.parseFloat(sigma) : 1.4f;
                Boolean auto = useAuto != null ? Boolean.parseBoolean(useAuto) : false;

                return imageProcessingService.processGaussian(file, kSize, s, auto)
                                .map(bytes -> ResponseEntity
                                                .ok()
                                                .contentType(MediaType.IMAGE_PNG)
                                                .body(bytes));
        }

        @PostMapping(value = "/negative", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.IMAGE_PNG_VALUE)
        public Mono<ResponseEntity<byte[]>> processNegative(
                        @RequestPart("file") FilePart file) {

                return imageProcessingService.processNegative(file)
                                .map(bytes -> ResponseEntity
                                                .ok()
                                                .contentType(MediaType.IMAGE_PNG)
                                                .body(bytes));
        }

        @PostMapping(value = "/emboss", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.IMAGE_PNG_VALUE)
        public Mono<ResponseEntity<byte[]>> processEmboss(
                        @RequestPart("file") FilePart file,
                        @RequestPart(value = "kernel_size", required = false) String kernelSize,
                        @RequestPart(value = "bias_value", required = false) String biasValue,
                        @RequestPart(value = "use_auto", required = false) String useAuto) {

                Integer kSize = kernelSize != null ? Integer.parseInt(kernelSize) : 3;
                Integer bias = biasValue != null ? Integer.parseInt(biasValue) : 128;
                Boolean auto = useAuto != null ? Boolean.parseBoolean(useAuto) : false;

                return imageProcessingService.processEmboss(file, kSize, bias, auto)
                                .map(bytes -> ResponseEntity
                                                .ok()
                                                .contentType(MediaType.IMAGE_PNG)
                                                .body(bytes));
        }

        @PostMapping(value = "/watermark", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.IMAGE_PNG_VALUE)
        public Mono<ResponseEntity<byte[]>> processWatermark(
                        @RequestPart("file") FilePart file,
                        @RequestPart(value = "scale", required = false) String scale,
                        @RequestPart(value = "transparency", required = false) String transparency,
                        @RequestPart(value = "spacing", required = false) String spacing) {

                Float sc = scale != null ? Float.parseFloat(scale) : 0.3f;
                Float tr = transparency != null ? Float.parseFloat(transparency) : 0.3f;
                Float sp = spacing != null ? Float.parseFloat(spacing) : 0.5f;

                return imageProcessingService.processWatermark(file, sc, tr, sp)
                                .map(bytes -> ResponseEntity
                                                .ok()
                                                .contentType(MediaType.IMAGE_PNG)
                                                .body(bytes));
        }

        @PostMapping(value = "/ripple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.IMAGE_PNG_VALUE)
        public Mono<ResponseEntity<byte[]>> processRipple(
                        @RequestPart("file") FilePart file,
                        @RequestPart(value = "edge_threshold", required = false) String edgeThreshold,
                        @RequestPart(value = "color_levels", required = false) String colorLevels,
                        @RequestPart(value = "saturation", required = false) String saturation) {

                Float edge = edgeThreshold != null ? Float.parseFloat(edgeThreshold) : 100.0f;
                Integer levels = colorLevels != null ? Integer.parseInt(colorLevels) : 8;
                Float sat = saturation != null ? Float.parseFloat(saturation) : 1.2f;

                return imageProcessingService.processRipple(file, edge, levels, sat)
                                .map(bytes -> ResponseEntity
                                                .ok()
                                                .contentType(MediaType.IMAGE_PNG)
                                                .body(bytes));
        }

        @PostMapping(value = "/collage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.IMAGE_PNG_VALUE)
        public Mono<ResponseEntity<byte[]>> processCollage(
                        @RequestPart("file") FilePart file) {

                return imageProcessingService.processCollage(file)
                                .map(bytes -> ResponseEntity
                                                .ok()
                                                .contentType(MediaType.IMAGE_PNG)
                                                .body(bytes));
        }
}
