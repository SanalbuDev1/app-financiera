package com.finanzas.personales.finanzas.auth.domain.model;

import lombok.Builder;
import lombok.Data;

/**
 * Modelo de dominio que representa a un usuario del sistema.
 * Es una entidad pura de dominio — no tiene anotaciones de frameworks
 * ni dependencias de infraestructura.
 */
@Data
@Builder
public class User {

    /** Identificador único del usuario (UUID como String). */
    private String id;

    /** Correo electrónico del usuario, usado como credencial de acceso. */
    private String email;

    /** Nombre completo del usuario. */
    private String name;

    /** Contraseña del usuario (hasheada con BCrypt en la capa de infraestructura). */
    private String password;

    /** Rol del usuario en el sistema (ADMIN o USER). */
    private UserRole role;
}
