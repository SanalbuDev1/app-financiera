package com.finanzas.personales.finanzas.auth.infrastructure.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO de entrada para la solicitud de autenticación (login).
 * Recibe el email y contraseña del usuario desde el frontend Angular.
 * Validado con Bean Validation antes de llegar al caso de uso.
 */
@Data
public class LoginRequest {

    /** Correo electrónico del usuario. Debe tener formato válido. */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;

    /** Contraseña en texto plano ingresada por el usuario. */
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
