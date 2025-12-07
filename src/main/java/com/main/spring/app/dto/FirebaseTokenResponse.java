package com.main.spring.app.dto;

import lombok.Data;

@Data
public class FirebaseTokenResponse {
    private String idToken; // El JWT que buscamos
    private String email;
    private String refreshToken;
    private String localId; // La UID del usuario
    private String expiresIn;
}
