package com.finanzas.personales.finanzas.deudas.infrastructure.adapter.out;

import com.finanzas.personales.finanzas.config.SqlQueryLoader;
import com.finanzas.personales.finanzas.deudas.domain.model.DebtType;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtTypeRepositoryPort;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adaptador de salida que implementa {@link DebtTypeRepositoryPort} usando R2DBC.
 * Gestiona el catálogo de tipos de deuda (tabla maestra administrable).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class R2dbcDebtTypeAdapter implements DebtTypeRepositoryPort {

    private final DatabaseClient databaseClient;
    private final SqlQueryLoader sqlQueryLoader;

    /**
     * Obtiene todos los tipos de deuda activos.
     *
     * @return {@code Flux<DebtType>} con los tipos activos
     */
    @Override
    public Flux<DebtType> findAllActive() {
        log.debug("[R2DBC] Listando tipos de deuda activos");
        String sql = sqlQueryLoader.load("deudas/listar_tipos_deuda");
        return databaseClient.sql(sql)
                .map(this::mapRowToDomain)
                .all();
    }

    /**
     * Obtiene todos los tipos de deuda (activos e inactivos).
     *
     * @return {@code Flux<DebtType>} con todos los tipos
     */
    @Override
    public Flux<DebtType> findAll() {
        log.debug("[R2DBC] Listando todos los tipos de deuda");
        return databaseClient.sql("SELECT * FROM debt_types ORDER BY name")
                .map(this::mapRowToDomain)
                .all();
    }

    /**
     * Busca un tipo de deuda por su ID.
     *
     * @param id identificador del tipo
     * @return {@code Mono<DebtType>} con el tipo, o vacío si no existe
     */
    @Override
    public Mono<DebtType> findById(String id) {
        log.debug("[R2DBC] Buscando tipo de deuda por id: {}", id);
        String sql = sqlQueryLoader.load("deudas/obtener_tipo_deuda_por_id");
        return databaseClient.sql(sql)
                .bind("id", id)
                .map(this::mapRowToDomain)
                .one();
    }

    /**
     * Busca un tipo de deuda por su nombre.
     *
     * @param name nombre del tipo
     * @return {@code Mono<DebtType>} con el tipo, o vacío si no existe
     */
    @Override
    public Mono<DebtType> findByName(String name) {
        log.debug("[R2DBC] Buscando tipo de deuda por nombre: {}", name);
        return databaseClient.sql("SELECT * FROM debt_types WHERE name = :name")
                .bind("name", name)
                .map(this::mapRowToDomain)
                .one();
    }

    /**
     * Guarda un nuevo tipo de deuda en la tabla maestra.
     *
     * @param debtType tipo a guardar
     * @return {@code Mono<DebtType>} con el tipo guardado
     */
    @Override
    public Mono<DebtType> save(DebtType debtType) {
        log.info("[R2DBC] Guardando tipo de deuda: {}", debtType.getName());
        return databaseClient.sql(
                        "INSERT INTO debt_types (id, name, description, icon, active) " +
                        "VALUES (:id, :name, :description, :icon, :active)")
                .bind("id", debtType.getId())
                .bind("name", debtType.getName())
                .bind("description", debtType.getDescription() != null ? debtType.getDescription() : "")
                .bind("icon", debtType.getIcon() != null ? debtType.getIcon() : "")
                .bind("active", debtType.getActive() != null ? debtType.getActive() : true)
                .fetch()
                .rowsUpdated()
                .then(findById(debtType.getId()));
    }

    /**
     * Actualiza un tipo de deuda existente.
     *
     * @param debtType tipo con los datos actualizados
     * @return {@code Mono<DebtType>} con el tipo actualizado
     */
    @Override
    public Mono<DebtType> update(DebtType debtType) {
        log.info("[R2DBC] Actualizando tipo de deuda id: {}", debtType.getId());
        return databaseClient.sql(
                        "UPDATE debt_types SET name = :name, description = :description, " +
                        "icon = :icon, active = :active WHERE id = :id")
                .bind("name", debtType.getName())
                .bind("description", debtType.getDescription() != null ? debtType.getDescription() : "")
                .bind("icon", debtType.getIcon() != null ? debtType.getIcon() : "")
                .bind("active", debtType.getActive() != null ? debtType.getActive() : true)
                .bind("id", debtType.getId())
                .fetch()
                .rowsUpdated()
                .then(findById(debtType.getId()));
    }

    /**
     * Desactiva (soft delete) un tipo de deuda por su ID.
     *
     * @param id identificador del tipo
     * @return {@code Mono<Void>} cuando la operación completa
     */
    @Override
    public Mono<Void> deactivate(String id) {
        log.info("[R2DBC] Desactivando tipo de deuda id: {}", id);
        return databaseClient.sql("UPDATE debt_types SET active = FALSE WHERE id = :id")
                .bind("id", id)
                .fetch()
                .rowsUpdated()
                .then();
    }

    /**
     * Verifica si existe un tipo de deuda con el nombre dado.
     *
     * @param name nombre a verificar
     * @return {@code Mono<Boolean>} true si existe, false si no
     */
    @Override
    public Mono<Boolean> existsByName(String name) {
        return databaseClient.sql(
                        "SELECT EXISTS(SELECT 1 FROM debt_types WHERE name = :name) AS exists")
                .bind("name", name)
                .map(row -> (Boolean) row.get("exists"))
                .one()
                .defaultIfEmpty(false);
    }

    /**
     * Mapea una fila del ResultSet al modelo de dominio {@link DebtType}.
     *
     * @param row fila del resultado SQL
     * @return objeto de dominio {@link DebtType}
     */
    private DebtType mapRowToDomain(Row row, RowMetadata metadata) {
        return DebtType.builder()
                .id((String) row.get("id"))
                .name((String) row.get("name"))
                .description((String) row.get("description"))
                .icon((String) row.get("icon"))
                .active((Boolean) row.get("active"))
                .build();
    }
}
