package com.main.spring.app.schema;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor

public class UserSchema {

    public String usr_username;
    public String usr_email;
    public String usr_photoUrl;
    public String usr_bio; // Usaremos el constructor para el valor por defecto

    // Constructor para la creación (opcional, si no usas @AllArgsConstructor)
    public UserSchema(String usr_username, String usr_email, String usr_photoUrl, String usr_bio) {
        this.usr_username = usr_username;
        this.usr_email = usr_email;
        this.usr_photoUrl = usr_photoUrl;
        this.usr_bio = (usr_bio != null && !usr_bio.trim().isEmpty()) ? usr_bio : "Hola, bienvenido a mi perfil";
    }
}
