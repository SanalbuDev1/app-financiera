package com.finanzas.personales.finanzas.auth.infrastructure.adapter.out;

import com.finanzas.personales.finanzas.auth.domain.model.User;
import com.finanzas.personales.finanzas.auth.domain.model.UserRole;
import com.finanzas.personales.finanzas.auth.domain.port.UserRepositoryPort;
import com.finanzas.personales.finanzas.config.SqlQueryLoader;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Adaptador de salida (output adapter) que implementa {@link UserRepositoryPort}
 * usando R2DBC para acceso reactivo a PostgreSQL.
 * Todos los queries SQL se cargan desde archivos externos en {@code resources/sql/auth/}
 * mediante {@link SqlQueryLoader}, manteniendo el SQL fuera del código Java.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class R2dbcUserAdapter implements UserRepositoryPort {

    /** Cliente de base de datos para ejecutar queries SQL reactivos. */
    private final DatabaseClient databaseClient;

    /** Cargador de queries SQL desde archivos del classpath. */
    private final SqlQueryLoader sqlQueryLoader;

    /**
     * Busca un usuario en la base de datos por su email.
     * Usa el query {@code sql/auth/buscar_usuario_por_email.sql}.
     *
     * @param email correo electrónico a buscar
     * @return {@code Mono<User>} con el usuario encontrado, o vacío si no existe
     */
    @Override
    public Mono<User> findByEmail(String email) {
        log.debug("[R2DBC] Buscando usuario por email: {}", email);
        String sql = sqlQueryLoader.load("auth/buscar_usuario_por_email");
        return databaseClient.sql(sql)
                .bind("email", email)
                .map(this::mapRowToDomain)
                .one()
                .doOnNext(user -> log.debug("[R2DBC] Usuario encontrado: {}", user.getEmail()))
                .switchIfEmpty(Mono.defer(() -> {
                    log.debug("[R2DBC] Usuario no encontrado para email: {}", email);
                    return Mono.empty();
                }));
    }

    /**
     * Persiste un usuario en la base de datos.
     * Usa el query {@code sql/auth/registrar_usuario.sql}.
     *
     * @param user modelo de dominio a guardar
     * @return {@code Mono<User>} con el usuario guardado
     */
    @Override
    public Mono<User> save(User user) {
        log.info("[R2DBC] Guardando nuevo usuario: {} (id: {})", user.getEmail(), user.getId());
        String sql = sqlQueryLoader.load("auth/registrar_usuario");
        return databaseClient.sql(sql)
                .bind("id", user.getId())
                .bind("email", user.getEmail())
                .bind("name", user.getName())
                .bind("password", user.getPassword())
                .bind("role", user.getRole().name())
                .fetch()
                .rowsUpdated()
                .doOnSuccess(count -> log.info("[R2DBC] Usuario guardado exitosamente: {}", user.getEmail()))
                .doOnError(ex -> log.error("[R2DBC] Error al guardar usuario {}: {}", user.getEmail(), ex.getMessage()))
                .thenReturn(user);
    }

    /**
     * Verifica si existe un usuario con el email dado.
     * Usa el query {@code sql/auth/verificar_existencia_email.sql}.
     *
     * @param email correo electrónico a verificar
     * @return {@code Mono<Boolean>} true si existe, false si no
     */
    @Override
    public Mono<Boolean> existsByEmail(String email) {
        log.debug("[R2DBC] Verificando existencia de email: {}", email);
        String sql = sqlQueryLoader.load("auth/verificar_existencia_email");
        return databaseClient.sql(sql)
                .bind("email", email)
                .map((row, metadata) -> row.get("exists_flag", Boolean.class))
                .one()
                .defaultIfEmpty(false)
                .doOnSuccess(exists -> log.debug("[R2DBC] Email {} existe: {}", email, exists));
    }

    // =========================================================
    // Mapper: Row → Domain
    // =========================================================

    /**
     * Mapea una fila del resultado SQL al modelo de dominio {@code User}.
     *
     * @param row      fila del resultado SQL
     * @param metadata metadata de la fila
     * @return modelo de dominio {@code User}
     */
    private User mapRowToDomain(Row row, RowMetadata metadata) {
        return User.builder()
                .id(row.get("id", String.class))
                .email(row.get("email", String.class))
                .name(row.get("name", String.class))
                .password(row.get("password", String.class))
                .role(UserRole.valueOf(row.get("role", String.class)))
                .build();
    }
}
