package com.main.spring.app.controller;

import com.main.spring.app.interfaces.users.UserService;
import com.main.spring.app.model.UpdateBioRequest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PatchMapping("/me/photo")
    @ResponseStatus(HttpStatus.OK)
    public Mono<String> updatePhoto(
            @RequestPart("photo") Mono<FilePart> filePartMono,
            Authentication authentication // JWT validado
    ) {
        // 1. Extraer UID del JWT
        String userId = (String) authentication.getPrincipal();

        // 2. Delegar al servicio
        return filePartMono
                .flatMap(filePart -> userService.updatePhoto(userId, filePart));
    }

    @PatchMapping("/me/bio")
    @ResponseStatus(HttpStatus.OK)
    public Mono<String> updateBio(
            @Valid @RequestBody UpdateBioRequest request,
            Authentication authentication // JWT validado
    ) {
        // 1. Extraer UID del JWT
        String userId = (String) authentication.getPrincipal();

        // 2. Delegar al servicio
        return userService.updateBio(userId, request.getUsr_bio());
    }
}

