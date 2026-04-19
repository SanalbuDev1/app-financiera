package com.finanzas.personales.finanzas.auth.infrastructure.entity;

import com.finanzas.personales.finanzas.auth.domain.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entidad R2DBC que representa la tabla {@code users} en PostgreSQL.
 * Pertenece exclusivamente a la capa de infraestructura — no se expone
 * fuera de ella. Se mapea hacia/desde el modelo de dominio {@code User}
 * en el adaptador de salida.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class UserEntity {

    /** Identificador único del usuario (UUID). */
    @Id
    private String id;

    /** Correo electrónico del usuario. Es único en la tabla. */
    private String email;

    /** Nombre completo del usuario. */
    private String name;

    /** Contraseña hasheada con BCrypt. */
    private String password;

    /** Rol del usuario: ADMIN o USER. Almacenado como String en la BD. */
    private UserRole role;
}
