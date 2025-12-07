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

    @PostMapping(value = "/canny", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<byte[]>> processCanny(
            @RequestPart("file") FilePart file,
            @RequestPart(value = "kernel_size", required = false) String kernelSize,
            @RequestPart(value = "sigma", required = false) String sigma,
            @RequestPart(value = "low_threshold", required = false) String lowThreshold,
            @RequestPart(value = "high_threshold", required = false) String highThreshold,
            @RequestPart(value = "use_auto", required = false) String useAuto) {

        // Parse parameters safely
        Integer kSize = kernelSize != null ? Integer.parseInt(kernelSize) : 5;
        Float s = sigma != null ? Float.parseFloat(sigma) : 1.4f;
        Boolean auto = useAuto != null ? Boolean.parseBoolean(useAuto) : false;

        return imageProcessingService.processCanny(file, kSize, s, lowThreshold, highThreshold, auto)
                .map(bytes -> ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .body(bytes));
    }
}
