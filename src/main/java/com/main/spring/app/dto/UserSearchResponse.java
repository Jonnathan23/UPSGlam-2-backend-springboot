package com.main.spring.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchResponse {

    private String uid;
    private String username;
    private String email;
    private String photoUrl;
}

