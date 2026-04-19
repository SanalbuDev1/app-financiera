package com.finanzas.personales.finanzas.auth.infrastructure.dto;

import com.finanzas.personales.finanzas.auth.domain.model.User;
import com.finanzas.personales.finanzas.auth.domain.model.UserRole;
import lombok.Builder;
import lombok.Data;

/**
 * DTO de salida que representa la respuesta de autenticación o registro exitoso.
 * Contiene los datos del usuario y el token JWT.
 * Este es el contrato que espera el frontend Angular.
 */
@Data
@Builder
public class UserResponse {

    /** Identificador único del usuario. */
    private String id;

    /** Correo electrónico del usuario. */
    private String email;

    /** Nombre completo del usuario. */
    private String name;

    /** Rol del usuario en el sistema (ADMIN o USER). */
    private UserRole role;

    /** Token JWT generado para la sesión del usuario. */
    private String token;

    /**
     * Construye un {@code UserResponse} a partir del modelo de dominio y el token JWT generado.
     *
     * @param user  modelo de dominio del usuario autenticado
     * @param token JWT generado por {@code JwtService}
     * @return DTO listo para serializar y enviar al frontend
     */
    public static UserResponse from(User user, String token) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .token(token)
                .build();
    }
}
