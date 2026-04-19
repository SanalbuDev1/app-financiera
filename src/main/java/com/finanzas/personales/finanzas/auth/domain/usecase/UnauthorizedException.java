package com.finanzas.personales.finanzas.auth.domain.usecase;

/**
 * Excepción de dominio lanzada cuando las credenciales de autenticación son inválidas.
 * Al ser una excepción de dominio puro, no depende de Spring ni de HTTP.
 * La capa de infraestructura la mapea al código HTTP 401 Unauthorized.
 */
public class UnauthorizedException extends RuntimeException {

    /**
     * Crea una nueva excepción con el mensaje descriptivo del error.
     *
     * @param message descripción del motivo del rechazo
     */
    public UnauthorizedException(String message) {
        super(message);
    }
}
