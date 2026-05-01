package com.finanzas.personales.finanzas.transacciones.infrastructure.adapter.out;

import com.finanzas.personales.finanzas.transacciones.domain.model.TransactionsDto;
import com.finanzas.personales.finanzas.transacciones.domain.model.port.TransactionRepositoryPort;
import com.finanzas.personales.finanzas.config.SqlQueryLoader;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Adaptador de salida (output adapter) que implementa {@link TransactionRepositoryPort}
 * usando R2DBC para acceso reactivo a PostgreSQL.
 * Todos los queries SQL se cargan desde archivos externos en {@code resources/sql/transactions/}
 * mediante {@link SqlQueryLoader}, manteniendo el SQL fuera del código Java.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class R2dbcTransactionAdapter implements TransactionRepositoryPort {

    /** Cliente de base de datos para ejecutar queries SQL reactivos. */
    private final DatabaseClient databaseClient;

    /** Cargador de queries SQL desde archivos del classpath. */
    private final SqlQueryLoader sqlQueryLoader;

    /**
     * Persiste una nueva transacción en la base de datos.
     * Resuelve category y type a sus FK correspondientes por convención de nombres.
     *
     * @param transaction transacción a guardar
     * @return {@code Mono<TransactionsDto>} con la transacción guardada (con nombres resueltos)
     */
    @Override
    public Mono<TransactionsDto> save(TransactionsDto transaction) {
        log.info("[R2DBC] Guardando transacción: {} (id: {})", transaction.getDescription(), transaction.getId());
        String sql = sqlQueryLoader.load("transactions/registrar_transaccion");
        return databaseClient.sql(sql)
                .bind("id", transaction.getId())
                .bind("userId", transaction.getUserId())
                .bind("description", transaction.getDescription())
                .bind("amount", transaction.getAmount())
                .bind("categoryId", "cat-" + transaction.getCategory())
                .bind("typeId", "type-" + transaction.getType())
                .bind("transactionDate", transaction.getTransactionDate())
                .bind("notes", transaction.getNotes() != null ? transaction.getNotes() : "")
                .bind("createdAt", transaction.getCreatedAt() != null ? transaction.getCreatedAt() : LocalDateTime.now())
                .fetch()
                .rowsUpdated()
                .doOnSuccess(count -> log.info("[R2DBC] Transacción insertada: {}", transaction.getId()))
                .doOnError(ex -> log.error("[R2DBC] Error al insertar transacción: {}", ex.getMessage()))
                .then(findById(transaction.getId()));
    }

    /**
     * Busca una transacción por su identificador único, resolviendo categoría y tipo con JOIN.
     *
     * @param transactionId UUID de la transacción
     * @return {@code Mono<TransactionsDto>} con la transacción encontrada, o vacío si no existe
     */
    @Override
    public Mono<TransactionsDto> findById(String transactionId) {
        log.debug("[R2DBC] Buscando transacción por id: {}", transactionId);
        String sql = sqlQueryLoader.load("transactions/obtener_transaccion_por_id");
        return databaseClient.sql(sql)
                .bind("id", transactionId)
                .map(this::mapRowToDomain)
                .one()
                .doOnNext(dto -> log.debug("[R2DBC] Transacción encontrada: {}", dto.getId()))
                .switchIfEmpty(Mono.defer(() -> {
                    log.debug("[R2DBC] Transacción no encontrada para id: {}", transactionId);
                    return Mono.empty();
                }));
    }

    /**
     * Obtiene todas las transacciones de un usuario sin paginación.
     *
     * @param userId identificador del usuario
     * @return {@code Flux<TransactionsDto>} con todas las transacciones del usuario
     */
    @Override
    public Flux<TransactionsDto> findAllByUserId(String userId) {
        log.debug("[R2DBC] Listando todas las transacciones - usuario: {}", userId);
        String sql = sqlQueryLoader.load("transactions/listar_transacciones_por_usuario");
        return databaseClient.sql(sql)
                .bind("userId", userId)
                .map(this::mapRowToDomain)
                .all();
    }

    /**
     * Obtiene transacciones paginadas con filtros dinámicos opcionales.
     *
     * @param userId   identificador del usuario
     * @param from     fecha inicio (inclusive), puede ser null
     * @param to       fecha fin (inclusive), puede ser null
     * @param type     nombre del tipo, puede ser null
     * @param category nombre de la categoría, puede ser null
     * @param offset   desplazamiento
     * @param limit    tamaño de página
     * @return {@code Flux<TransactionsDto>} paginado
     */
    @Override
    public Flux<TransactionsDto> findByUserIdPaginated(String userId, LocalDate from, LocalDate to,
                                                        String type, String category, int offset, int limit) {
        log.debug("[R2DBC] Listando transacciones paginadas - usuario: {}, page offset: {}, limit: {}", userId, offset, limit);
        String sql = sqlQueryLoader.load("transactions/listar_transacciones_paginadas");
        var spec = databaseClient.sql(sql)
                .bind("userId", userId);
        // R2DBC requiere bindNull en lugar de bind(null) para parámetros opcionales
        spec = from != null ? spec.bind("fromDate", from) : spec.bindNull("fromDate", LocalDate.class);
        spec = to != null ? spec.bind("toDate", to) : spec.bindNull("toDate", LocalDate.class);
        spec = type != null ? spec.bind("typeName", type) : spec.bindNull("typeName", String.class);
        spec = category != null ? spec.bind("categoryName", category) : spec.bindNull("categoryName", String.class);
        return spec.bind("offset", offset)
                .bind("limit", limit)
                .map(this::mapRowToDomain)
                .all();
    }

    /**
     * Cuenta el total de transacciones que coinciden con los filtros.
     *
     * @param userId   identificador del usuario
     * @param from     fecha inicio, puede ser null
     * @param to       fecha fin, puede ser null
     * @param type     tipo, puede ser null
     * @param category categoría, puede ser null
     * @return {@code Mono<Long>} total de registros
     */
    @Override
    public Mono<Long> countByUserIdFiltered(String userId, LocalDate from, LocalDate to,
                                             String type, String category) {
        log.debug("[R2DBC] Contando transacciones filtradas - usuario: {}", userId);
        String sql = sqlQueryLoader.load("transactions/contar_transacciones_filtradas");
        var spec = databaseClient.sql(sql)
                .bind("userId", userId);
        // R2DBC requiere bindNull en lugar de bind(null) para parámetros opcionales
        spec = from != null ? spec.bind("fromDate", from) : spec.bindNull("fromDate", LocalDate.class);
        spec = to != null ? spec.bind("toDate", to) : spec.bindNull("toDate", LocalDate.class);
        spec = type != null ? spec.bind("typeName", type) : spec.bindNull("typeName", String.class);
        spec = category != null ? spec.bind("categoryName", category) : spec.bindNull("categoryName", String.class);
        return spec.map((row, metadata) -> row.get("total", Long.class))
                .one()
                .defaultIfEmpty(0L);
    }

    /**
     * Calcula el balance total histórico del usuario.
     *
     * @param userId identificador del usuario
     * @return {@code Mono<BigDecimal>} balance total
     */
    @Override
    public Mono<BigDecimal> getTotalBalance(String userId) {
        log.debug("[R2DBC] Calculando balance total - usuario: {}", userId);
        String sql = sqlQueryLoader.load("transactions/obtener_balance_total");
        return databaseClient.sql(sql)
                .bind("userId", userId)
                .map((row, metadata) -> row.get("total_balance", BigDecimal.class))
                .one()
                .defaultIfEmpty(BigDecimal.ZERO);
    }

    /**
     * Calcula la suma de ingresos de un mes/año específico.
     *
     * @param userId identificador del usuario
     * @param month  mes (1-12)
     * @param year   año
     * @return {@code Mono<BigDecimal>} suma de ingresos
     */
    @Override
    public Mono<BigDecimal> getMonthlyIncome(String userId, int month, int year) {
        log.debug("[R2DBC] Calculando ingresos mensuales - usuario: {}, mes: {}/{}", userId, month, year);
        String sql = sqlQueryLoader.load("transactions/obtener_ingresos_mensuales");
        return databaseClient.sql(sql)
                .bind("userId", userId)
                .bind("month", month)
                .bind("year", year)
                .map((row, metadata) -> row.get("monthly_income", BigDecimal.class))
                .one()
                .defaultIfEmpty(BigDecimal.ZERO);
    }

    /**
     * Calcula la suma de gastos de un mes/año específico.
     *
     * @param userId identificador del usuario
     * @param month  mes (1-12)
     * @param year   año
     * @return {@code Mono<BigDecimal>} suma de gastos
     */
    @Override
    public Mono<BigDecimal> getMonthlyExpenses(String userId, int month, int year) {
        log.debug("[R2DBC] Calculando gastos mensuales - usuario: {}, mes: {}/{}", userId, month, year);
        String sql = sqlQueryLoader.load("transactions/obtener_gastos_mensuales");
        return databaseClient.sql(sql)
                .bind("userId", userId)
                .bind("month", month)
                .bind("year", year)
                .map((row, metadata) -> row.get("monthly_expenses", BigDecimal.class))
                .one()
                .defaultIfEmpty(BigDecimal.ZERO);
    }

    /**
     * Elimina una transacción solo si pertenece al usuario indicado.
     *
     * @param transactionId UUID de la transacción
     * @param userId        UUID del dueño
     * @return {@code Mono<Long>} filas eliminadas (0 o 1)
     */
    @Override
    public Mono<Long> deleteByIdAndUserId(String transactionId, String userId) {
        log.info("[R2DBC] Eliminando transacción: {} del usuario: {}", transactionId, userId);
        String sql = sqlQueryLoader.load("transactions/eliminar_transaccion_por_id_y_usuario");
        return databaseClient.sql(sql)
                .bind("id", transactionId)
                .bind("userId", userId)
                .fetch()
                .rowsUpdated()
                .doOnSuccess(count -> log.info("[R2DBC] Transacción eliminada, filas afectadas: {}", count));
    }

    /**
     * Verifica si existe una transacción con el ID dado.
     *
     * @param transactionId UUID de la transacción
     * @return {@code Mono<Boolean>} true si existe, false si no
     */
    @Override
    public Mono<Boolean> existsById(String transactionId) {
        log.debug("[R2DBC] Verificando existencia de transacción: {}", transactionId);
        String sql = sqlQueryLoader.load("transactions/verificar_existencia_transaccion");
        return databaseClient.sql(sql)
                .bind("id", transactionId)
                .map((row, metadata) -> row.get("exists_flag", Boolean.class))
                .one()
                .defaultIfEmpty(false)
                .doOnSuccess(exists -> log.debug("[R2DBC] Transacción {} existe: {}", transactionId, exists));
    }

    /**
     * Actualiza una transacción existente que pertenece al usuario indicado.
     * Solo actualiza si el user_id coincide con el dueño.
     *
     * @param transaction transacción con los datos actualizados
     * @return {@code Mono<Long>} con la cantidad de filas actualizadas (0 o 1)
     */
    @Override
    public Mono<Long> update(TransactionsDto transaction) {
        log.info("[R2DBC] Actualizando transacción: {} del usuario: {}", transaction.getId(), transaction.getUserId());
        String sql = sqlQueryLoader.load("transactions/actualizar_transaccion");
        return databaseClient.sql(sql)
                .bind("id", transaction.getId())
                .bind("userId", transaction.getUserId())
                .bind("description", transaction.getDescription())
                .bind("amount", transaction.getAmount())
                .bind("categoryId", "cat-" + transaction.getCategory())
                .bind("typeId", "type-" + transaction.getType())
                .bind("transactionDate", transaction.getTransactionDate())
                .bind("notes", transaction.getNotes() != null ? transaction.getNotes() : "")
                .fetch()
                .rowsUpdated()
                .doOnSuccess(count -> log.info("[R2DBC] Transacción actualizada, filas afectadas: {}", count));
    }

    // =========================================================
    // Mapper: Row (JOIN result) → Domain
    // =========================================================

    /**
     * Mapea una fila del resultado SQL (con JOIN) al modelo de dominio.
     *
     * @param row      fila del resultado SQL
     * @param metadata metadata de la fila
     * @return modelo de dominio {@code TransactionsDto}
     */
    private TransactionsDto mapRowToDomain(Row row, RowMetadata metadata) {
        return TransactionsDto.builder()
                .id(row.get("id", String.class))
                .userId(row.get("user_id", String.class))
                .description(row.get("description", String.class))
                .amount(row.get("amount", BigDecimal.class))
                .category(row.get("category_name", String.class))
                .type(row.get("type_name", String.class))
                .transactionDate(row.get("transaction_date", LocalDate.class))
                .notes(row.get("notes", String.class))
                .createdAt(row.get("created_at", LocalDateTime.class))
                .build();
    }
}
