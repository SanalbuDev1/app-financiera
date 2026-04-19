package com.finanzas.personales.finanzas.auth.infrastructure.adapter.out;

import com.finanzas.personales.finanzas.auth.domain.model.User;
import com.finanzas.personales.finanzas.auth.domain.model.UserRole;
import com.finanzas.personales.finanzas.auth.domain.port.UserRepositoryPort;
import com.finanzas.personales.finanzas.auth.infrastructure.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Adaptador de salida (output adapter) que implementa {@link UserRepositoryPort}
 * usando R2DBC para acceso reactivo a PostgreSQL.
 * Traduce entre el modelo de dominio {@code User} y la entidad de infraestructura {@code UserEntity}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class R2dbcUserAdapter implements UserRepositoryPort {

    /** Template R2DBC para ejecutar operaciones reactivas sobre la base de datos. */
    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    /**
     * Busca un usuario en la base de datos por su email.
     *
     * @param email correo electrónico a buscar
     * @return {@code Mono<User>} con el usuario encontrado, o vacío si no existe
     */
    @Override
    public Mono<User> findByEmail(String email) {
        log.debug("[R2DBC] Buscando usuario por email: {}", email);
        return r2dbcEntityTemplate
                .selectOne(Query.query(Criteria.where("email").is(email)), UserEntity.class)
                .doOnNext(entity -> log.debug("[R2DBC] Usuario encontrado: {}", entity.getEmail()))
                .switchIfEmpty(Mono.defer(() -> {
                    log.debug("[R2DBC] Usuario no encontrado para email: {}", email);
                    return Mono.empty();
                }))
                .map(this::toDomain);
    }

    /**
     * Persiste un usuario en la base de datos.
     *
     * @param user modelo de dominio a guardar
     * @return {@code Mono<User>} con el usuario guardado
     */
    @Override
    public Mono<User> save(User user) {
        log.info("[R2DBC] Guardando nuevo usuario: {} (id: {})", user.getEmail(), user.getId());
        return r2dbcEntityTemplate
                .insert(toEntity(user))
                .doOnSuccess(entity -> log.info("[R2DBC] Usuario guardado exitosamente: {}", entity.getEmail()))
                .doOnError(ex -> log.error("[R2DBC] Error al guardar usuario {}: {}", user.getEmail(), ex.getMessage()))
                .map(this::toDomain);
    }

    /**
     * Verifica si existe un usuario con el email dado.
     *
     * @param email correo electrónico a verificar
     * @return {@code Mono<Boolean>} true si existe, false si no
     */
    @Override
    public Mono<Boolean> existsByEmail(String email) {
        log.debug("[R2DBC] Verificando existencia de email: {}", email);
        return r2dbcEntityTemplate
                .exists(Query.query(Criteria.where("email").is(email)), UserEntity.class)
                .doOnSuccess(exists -> log.debug("[R2DBC] Email {} existe: {}", email, exists));
    }

    /**
     * Convierte una entidad de infraestructura al modelo de dominio.
     *
     * @param entity entidad R2DBC leída de la base de datos
     * @return modelo de dominio {@code User}
     */
    private User toDomain(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .name(entity.getName())
                .password(entity.getPassword())
                .role(entity.getRole())
                .build();
    }

    /**
     * Convierte un modelo de dominio a entidad de infraestructura para persistir.
     *
     * @param user modelo de dominio
     * @return entidad R2DBC lista para insertar
     */
    private UserEntity toEntity(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .password(user.getPassword())
                .role(user.getRole())
                .build();
    }
}
