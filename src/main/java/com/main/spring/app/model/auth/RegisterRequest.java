package com.main.spring.app.model.auth;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.AssertTrue;

@Data
public class RegisterRequest {

    @NotBlank(message = "El nombre no puede estar vacío")
    private String usr_username;

    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "Email inválido")
    private String usr_email;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String usr_password;

    @NotBlank(message = "La contraseña de confirmación no puede estar vacía")
    private String usr_confirmPassword;

    @NotBlank(message = "La URL de la foto no puede estar vacía")
    private String usr_photoUrl;

    private String usr_bio = "Hola, bienvenido a mi perfil";

    @AssertTrue(message = "Las contraseñas no coinciden")
    public boolean isPasswordConfirmed() {
        if (usr_password == null || usr_confirmPassword == null) {
            return false;
        }
        return usr_password.equals(usr_confirmPassword);
    }   

}
