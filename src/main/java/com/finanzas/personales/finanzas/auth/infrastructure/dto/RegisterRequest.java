package com.finanzas.personales.finanzas.auth.infrastructure.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO de entrada para la solicitud de registro de un nuevo usuario.
 * Recibe email, contraseña y nombre desde el frontend Angular.
 * Validado con Bean Validation antes de llegar al caso de uso.
 */
@Data
public class RegisterRequest {

    /** Correo electrónico del nuevo usuario. Debe tener formato válido y ser único. */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;

    /** Contraseña en texto plano. Mínimo 6 caracteres. */
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    /** Nombre completo del usuario. */
    @NotBlank(message = "El nombre es obligatorio")
    private String name;
}
