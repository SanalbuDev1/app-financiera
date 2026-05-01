package com.finanzas.personales.finanzas.security.domain.port;

import com.finanzas.personales.finanzas.auth.domain.model.User;

/**
 * Puerto del dominio que define el contrato para operaciones con tokens de autenticación.
 * Permite desacoplar la lógica de negocio de la implementación concreta del token (JWT, etc.).
 * La infraestructura implementa este puerto con la tecnología elegida (JJWT en este caso).
 */
public interface TokenServicePort {

    /**
     * Genera un token de autenticación para el usuario proporcionado.
     *
     * @param user modelo de dominio del usuario autenticado
     * @return token firmado como String
     */
    String generateToken(User user);

    /**
     * Verifica si un token es válido (firma correcta y no expirado).
     *
     * @param token token a validar
     * @return {@code true} si el token es válido, {@code false} si no
     */
    boolean isTokenValid(String token);

    /**
     * Extrae el identificador único (UUID) del usuario del token.
     *
     * @param token token de autenticación
     * @return id del usuario como String
     */
    String extractId(String token);

    /**
     * Extrae el email (subject) del token.
     *
     * @param token token de autenticación
     * @return email del usuario
     */
    String extractEmail(String token);

    /**
     * Extrae el rol del usuario del token.
     *
     * @param token token de autenticación
     * @return rol como String (ej. "ADMIN", "USER")
     */
    String extractRole(String token);
}
