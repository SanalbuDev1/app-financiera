package com.finanzas.personales.finanzas.auth.domain.usecase;

/**
 * Excepción de dominio lanzada cuando se intenta registrar un usuario
 * con un email que ya existe en el sistema.
 * La capa de infraestructura la mapea al código HTTP 409 Conflict.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    /**
     * Crea una nueva excepción con el mensaje descriptivo del error.
     *
     * @param message descripción del motivo del conflicto
     */
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
