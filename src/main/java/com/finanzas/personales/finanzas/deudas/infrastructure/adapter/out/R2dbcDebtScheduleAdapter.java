package com.finanzas.personales.finanzas.deudas.infrastructure.adapter.out;

import com.finanzas.personales.finanzas.config.SqlQueryLoader;
import com.finanzas.personales.finanzas.deudas.domain.model.DebtScheduleItem;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtScheduleRepositoryPort;
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
import java.util.List;

/**
 * Adaptador de salida que implementa {@link DebtScheduleRepositoryPort} usando R2DBC.
 * Gestiona la tabla de amortización (cronograma de cuotas) de las deudas.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class R2dbcDebtScheduleAdapter implements DebtScheduleRepositoryPort {

    private final DatabaseClient databaseClient;
    private final SqlQueryLoader sqlQueryLoader;

    /**
     * Guarda todos los ítems del cronograma de amortización, uno por uno en secuencia reactiva.
     *
     * @param items lista de ítems a persistir
     * @return {@code Flux<DebtScheduleItem>} con los ítems guardados
     */
    @Override
    public Flux<DebtScheduleItem> saveAll(List<DebtScheduleItem> items) {
        log.info("[R2DBC] Guardando {} ítems de cronograma", items.size());
        String sql = sqlQueryLoader.load("deudas/registrar_item_cronograma");
        return Flux.fromIterable(items)
                .concatMap(item -> databaseClient.sql(sql)
                        .bind("id", item.getId())
                        .bind("debtId", item.getDebtId())
                        .bind("installmentNumber", item.getInstallmentNumber())
                        .bind("dueDate", item.getDueDate())
                        .bind("principalAmount", item.getPrincipalAmount())
                        .bind("interestAmount", item.getInterestAmount())
                        .bind("totalAmount", item.getTotalAmount())
                        .bind("balanceAfter", item.getBalanceAfter())
                        .bind("status", item.getStatus())
                        .bind("createdAt", item.getCreatedAt() != null ? item.getCreatedAt() : LocalDateTime.now())
                        .fetch()
                        .rowsUpdated()
                        .thenReturn(item));
    }

    /**
     * Obtiene el cronograma completo de una deuda ordenado por número de cuota.
     *
     * @param debtId identificador de la deuda
     * @return {@code Flux<DebtScheduleItem>} con los ítems ordenados
     */
    @Override
    public Flux<DebtScheduleItem> findByDebtId(String debtId) {
        log.debug("[R2DBC] Obteniendo cronograma para deuda id: {}", debtId);
        String sql = sqlQueryLoader.load("deudas/listar_cronograma_por_deuda");
        return databaseClient.sql(sql)
                .bind("debtId", debtId)
                .map(this::mapRowToDomain)
                .all();
    }

    /**
     * Obtiene las cuotas pendientes de una deuda.
     *
     * @param debtId identificador de la deuda
     * @return {@code Flux<DebtScheduleItem>} con las cuotas pendientes
     */
    @Override
    public Flux<DebtScheduleItem> findPendingByDebtId(String debtId) {
        log.debug("[R2DBC] Obteniendo cuotas pendientes para deuda id: {}", debtId);
        // Reutiliza el query de cronograma filtrado en memoria — suficiente para cantidades pequeñas
        return findByDebtId(debtId)
                .filter(item -> "pending".equals(item.getStatus()));
    }

    /**
     * Obtiene la próxima cuota pendiente (la de menor número de cuota).
     *
     * @param debtId identificador de la deuda
     * @return {@code Mono<DebtScheduleItem>} con la próxima cuota, o vacío si no hay pendientes
     */
    @Override
    public Mono<DebtScheduleItem> findNextPendingByDebtId(String debtId) {
        log.debug("[R2DBC] Obteniendo próxima cuota pendiente para deuda id: {}", debtId);
        String sql = sqlQueryLoader.load("deudas/obtener_proxima_cuota_pendiente");
        return databaseClient.sql(sql)
                .bind("debtId", debtId)
                .map(this::mapRowToDomain)
                .one();
    }

    /**
     * Busca cuotas que vencen en o antes de una fecha específica, para un usuario dado.
     *
     * @param userId  identificador del usuario
     * @param dueDate fecha límite de vencimiento
     * @return {@code Flux<DebtScheduleItem>} con las cuotas próximas a vencer
     */
    @Override
    public Flux<DebtScheduleItem> findUpcomingByUserIdAndDueDate(String userId, LocalDate dueDate) {
        log.debug("[R2DBC] Buscando cuotas con vencimiento antes de {} para usuario: {}", dueDate, userId);
        return databaseClient.sql(
                        """
                        SELECT ds.* FROM debt_schedule ds
                        JOIN debts d ON d.id = ds.debt_id
                        WHERE d.user_id = :userId
                          AND ds.status = 'pending'
                          AND ds.due_date <= :dueDate
                        ORDER BY ds.due_date ASC
                        """)
                .bind("userId", userId)
                .bind("dueDate", dueDate)
                .map(this::mapRowToDomain)
                .all();
    }

    /**
     * Actualiza el estado de un ítem del cronograma.
     *
     * @param id     identificador del ítem
     * @param status nuevo estado
     * @return {@code Mono<DebtScheduleItem>} con el ítem actualizado
     */
    @Override
    public Mono<DebtScheduleItem> updateStatus(String id, String status) {
        log.info("[R2DBC] Actualizando estado de cuota id: {} → {}", id, status);
        String sql = sqlQueryLoader.load("deudas/actualizar_estado_cuota");
        return databaseClient.sql(sql)
                .bind("status", status)
                .bind("id", id)
                .fetch()
                .rowsUpdated()
                .then(databaseClient.sql("SELECT * FROM debt_schedule WHERE id = :id")
                        .bind("id", id)
                        .map(this::mapRowToDomain)
                        .one());
    }

    /**
     * Elimina todos los ítems del cronograma de una deuda.
     *
     * @param debtId identificador de la deuda
     * @return {@code Mono<Void>} cuando la operación completa
     */
    @Override
    public Mono<Void> deleteByDebtId(String debtId) {
        log.info("[R2DBC] Eliminando cronograma de deuda id: {}", debtId);
        String sql = sqlQueryLoader.load("deudas/eliminar_cronograma_pendiente_por_deuda");
        return databaseClient.sql(sql)
                .bind("debtId", debtId)
                .fetch()
                .rowsUpdated()
                .then();
    }

    /**
     * Regenera el cronograma: elimina los pendientes y guarda los nuevos ítems.
     *
     * @param debtId identificador de la deuda
     * @param items  nuevos ítems del cronograma
     * @return {@code Flux<DebtScheduleItem>} con los nuevos ítems guardados
     */
    @Override
    public Flux<DebtScheduleItem> regenerateSchedule(String debtId, List<DebtScheduleItem> items) {
        log.info("[R2DBC] Regenerando cronograma de deuda id: {} con {} cuotas", debtId, items.size());
        return deleteByDebtId(debtId)
                .thenMany(saveAll(items));
    }

    /**
     * Mapea una fila del ResultSet al modelo de dominio {@link DebtScheduleItem}.
     *
     * @param row fila del resultado SQL
     * @return objeto de dominio {@link DebtScheduleItem}
     */
    private DebtScheduleItem mapRowToDomain(Row row, RowMetadata metadata) {
        return DebtScheduleItem.builder()
                .id((String) row.get("id"))
                .debtId((String) row.get("debt_id"))
                .installmentNumber((Integer) row.get("installment_number"))
                .dueDate(row.get("due_date", LocalDate.class))
                .principalAmount((BigDecimal) row.get("principal_amount"))
                .interestAmount((BigDecimal) row.get("interest_amount"))
                .totalAmount((BigDecimal) row.get("total_amount"))
                .balanceAfter((BigDecimal) row.get("balance_after"))
                .status((String) row.get("status"))
                .createdAt(row.get("created_at", LocalDateTime.class))
                .build();
    }
}
