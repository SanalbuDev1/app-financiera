package com.finanzas.personales.finanzas.auth.domain.port;

import com.finanzas.personales.finanzas.auth.domain.model.User;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida (output port) del dominio para operaciones de persistencia de usuarios.
 * Define el contrato que debe cumplir cualquier adaptador de base de datos.
 * La capa de dominio depende de esta interfaz; la infraestructura la implementa.
 */
public interface UserRepositoryPort {

    /**
     * Busca un usuario por su dirección de correo electrónico.
     *
     * @param email correo electrónico a buscar
     * @return {@code Mono<User>} con el usuario encontrado, o vacío si no existe
     */
    Mono<User> findByEmail(String email);

    /**
     * Persiste un nuevo usuario en el almacenamiento.
     *
     * @param user usuario a guardar (con contraseña ya hasheada)
     * @return {@code Mono<User>} con el usuario guardado, incluyendo el ID generado
     */
    Mono<User> save(User user);

    /**
     * Verifica si ya existe un usuario con el email dado.
     *
     * @param email correo electrónico a verificar
     * @return {@code Mono<Boolean>} true si existe, false si no
     */
    Mono<Boolean> existsByEmail(String email);
}
