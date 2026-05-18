package com.finanzas.personales.finanzas.deudas.infrastructure.adapter.out;

import com.finanzas.personales.finanzas.config.SqlQueryLoader;
import com.finanzas.personales.finanzas.deudas.domain.model.Debt;
import com.finanzas.personales.finanzas.deudas.domain.model.DebtSummary;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtRepositoryPort;
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
 * Adaptador de salida (output adapter) que implementa {@link DebtRepositoryPort}
 * usando R2DBC para acceso reactivo a PostgreSQL.
 * Todos los queries SQL se cargan desde archivos externos en {@code resources/sql/deudas/}
 * mediante {@link SqlQueryLoader}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class R2dbcDebtAdapter implements DebtRepositoryPort {

    private final DatabaseClient databaseClient;
    private final SqlQueryLoader sqlQueryLoader;

    /**
     * Persiste una nueva deuda y la retorna con datos completos (JOIN con tablas maestras).
     *
     * @param debt deuda a guardar
     * @return {@code Mono<Debt>} con la deuda guardada
     */
    @Override
    public Mono<Debt> save(Debt debt) {
        log.info("[R2DBC] Guardando deuda: {} para usuario: {}", debt.getCreditor(), debt.getUserId());
        String sql = sqlQueryLoader.load("deudas/registrar_deuda");
        return databaseClient.sql(sql)
                .bind("id", debt.getId())
                .bind("userId", debt.getUserId())
                .bind("debtTypeId", debt.getDebtTypeId())
                .bind("frequencyId", debt.getFrequencyId())
                .bind("creditor", debt.getCreditor())
                .bind("description", debt.getDescription())
                .bind("originalAmount", debt.getOriginalAmount())
                .bind("currentBalance", debt.getCurrentBalance())
                .bind("interestRate", debt.getInterestRate())
                .bind("interestRateType", debt.getInterestRateType())
                .bind("totalInstallments", debt.getTotalInstallments())
                .bind("remainingInstallments", debt.getRemainingInstallments())
                .bind("installmentAmount", debt.getInstallmentAmount())
                .bind("startDate", debt.getStartDate())
                .bind("nextPaymentDate", debt.getNextPaymentDate())
                .bind("status", debt.getStatus())
                .bind("notes", debt.getNotes() != null ? debt.getNotes() : "")
                .bind("createdAt", debt.getCreatedAt() != null ? debt.getCreatedAt() : LocalDateTime.now())
                .fetch()
                .rowsUpdated()
                .then(findById(debt.getId()));
    }

    /**
     * Actualiza los campos modificables de una deuda existente.
     *
     * @param debt deuda con los datos actualizados
     * @return {@code Mono<Debt>} con la deuda actualizada
     */
    @Override
    public Mono<Debt> update(Debt debt) {
        log.info("[R2DBC] Actualizando deuda id: {}", debt.getId());
        String sql = sqlQueryLoader.load("deudas/actualizar_deuda");
        var spec = databaseClient.sql(sql)
                .bind("creditor", debt.getCreditor())
                .bind("description", debt.getDescription())
                .bind("currentBalance", debt.getCurrentBalance())
                .bind("interestRate", debt.getInterestRate())
                .bind("interestRateType", debt.getInterestRateType())
                .bind("remainingInstallments", debt.getRemainingInstallments())
                .bind("installmentAmount", debt.getInstallmentAmount());

        // nextPaymentDate es NULL cuando la deuda queda saldada (paid_off)
        if (debt.getNextPaymentDate() != null) {
            spec = spec.bind("nextPaymentDate", debt.getNextPaymentDate());
        } else {
            spec = spec.bindNull("nextPaymentDate", java.time.LocalDate.class);
        }

        return spec
                .bind("status", debt.getStatus())
                .bind("notes", debt.getNotes() != null ? debt.getNotes() : "")
                .bind("id", debt.getId())
                .fetch()
                .rowsUpdated()
                .thenReturn(debt); // retornamos el objeto ya actualizado, evitando Mono.empty() del SELECT
    }

    /**
     * Busca una deuda por su ID con JOIN a tablas maestras.
     *
     * @param id identificador de la deuda
     * @return {@code Mono<Debt>} con la deuda encontrada, o vacío si no existe
     */
    @Override
    public Mono<Debt> findById(String id) {
        log.debug("[R2DBC] Buscando deuda por id: {}", id);
        String sql = sqlQueryLoader.load("deudas/obtener_deuda_por_id");
        return databaseClient.sql(sql)
                .bind("id", id)
                .map(this::mapRowToDomain)
                .one();
    }

    /**
     * Busca una deuda verificando que pertenezca al usuario.
     *
     * @param id     identificador de la deuda
     * @param userId identificador del usuario
     * @return {@code Mono<Debt>} con la deuda, o vacío si no existe o no pertenece al usuario
     */
    @Override
    public Mono<Debt> findByIdAndUserId(String id, String userId) {
        log.debug("[R2DBC] Buscando deuda id: {} para usuario: {}", id, userId);
        String sql = sqlQueryLoader.load("deudas/obtener_deuda_por_id_y_usuario");
        return databaseClient.sql(sql)
                .bind("id", id)
                .bind("userId", userId)
                .map(this::mapRowToDomain)
                .one();
    }

    /**
     * Obtiene todas las deudas de un usuario, ordenadas por fecha de creación descendente.
     *
     * @param userId identificador del usuario
     * @return {@code Flux<Debt>} con las deudas del usuario
     */
    @Override
    public Flux<Debt> findAllByUserId(String userId) {
        log.debug("[R2DBC] Listando deudas para usuario: {}", userId);
        String sql = sqlQueryLoader.load("deudas/listar_deudas_por_usuario");
        return databaseClient.sql(sql)
                .bind("userId", userId)
                .map(this::mapRowToDomain)
                .all();
    }

    /**
     * Obtiene las deudas activas de un usuario.
     *
     * @param userId identificador del usuario
     * @return {@code Flux<Debt>} con las deudas con status='active'
     */
    @Override
    public Flux<Debt> findActiveByUserId(String userId) {
        return findByUserIdAndStatus(userId, "active");
    }

    /**
     * Obtiene las deudas de un usuario filtradas por estado.
     *
     * @param userId identificador del usuario
     * @param status estado de la deuda
     * @return {@code Flux<Debt>} con las deudas filtradas
     */
    @Override
    public Flux<Debt> findByUserIdAndStatus(String userId, String status) {
        log.debug("[R2DBC] Listando deudas usuario: {} con status: {}", userId, status);
        String sql = sqlQueryLoader.load("deudas/listar_deudas_por_usuario_y_estado");
        return databaseClient.sql(sql)
                .bind("userId", userId)
                .bind("status", status)
                .map(this::mapRowToDomain)
                .all();
    }

    /**
     * Obtiene el resumen financiero de las deudas activas del usuario.
     *
     * @param userId identificador del usuario
     * @return {@code Mono<DebtSummary>} con el resumen agregado
     */
    @Override
    public Mono<DebtSummary> getSummaryByUserId(String userId) {
        log.debug("[R2DBC] Obteniendo resumen de deudas para usuario: {}", userId);
        String sql = sqlQueryLoader.load("deudas/obtener_resumen_deudas");
        return databaseClient.sql(sql)
                .bind("userId", userId)
                .map(row -> DebtSummary.builder()
                        .totalDebts(((Number) row.get("total_debts")).intValue())
                        .totalBalance((BigDecimal) row.get("total_balance"))
                        .totalOriginalAmount((BigDecimal) row.get("total_original_amount"))
                        .totalMonthlyPayment((BigDecimal) row.get("total_monthly_payment"))
                        .totalPendingInterest((BigDecimal) row.get("total_pending_interest"))
                        .averageProgress((BigDecimal) row.get("average_progress"))
                        .build())
                .one();
    }

    /**
     * Elimina una deuda por su ID (el CASCADE en BD elimina schedule y payments).
     *
     * @param id identificador de la deuda
     * @return {@code Mono<Void>} cuando la operación completa
     */
    @Override
    public Mono<Void> deleteById(String id) {
        log.info("[R2DBC] Eliminando deuda id: {}", id);
        String sql = sqlQueryLoader.load("deudas/eliminar_deuda_por_id");
        return databaseClient.sql(sql)
                .bind("id", id)
                .fetch()
                .rowsUpdated()
                .then();
    }

    /**
     * Verifica si existe una deuda con el ID dado.
     *
     * @param id identificador de la deuda
     * @return {@code Mono<Boolean>} true si existe, false si no
     */
    @Override
    public Mono<Boolean> existsById(String id) {
        String sql = sqlQueryLoader.load("deudas/verificar_existencia_deuda");
        return databaseClient.sql(sql)
                .bind("id", id)
                .map(row -> (Boolean) row.get("exists"))
                .one()
                .defaultIfEmpty(false);
    }

    /**
     * Mapea una fila del ResultSet al modelo de dominio {@link Debt}.
     * Incluye los campos resueltos por JOIN (debtTypeName, frequencyName).
     *
     * @param row fila del resultado SQL
     * @return objeto de dominio {@link Debt}
     */
    private Debt mapRowToDomain(Row row, RowMetadata metadata) {
        return Debt.builder()
                .id((String) row.get("id"))
                .userId((String) row.get("user_id"))
                .debtTypeId((String) row.get("debt_type_id"))
                .debtTypeName((String) row.get("debt_type_name"))
                .frequencyId((String) row.get("frequency_id"))
                .frequencyName((String) row.get("frequency_name"))
                .creditor((String) row.get("creditor"))
                .description((String) row.get("description"))
                .originalAmount((BigDecimal) row.get("original_amount"))
                .currentBalance((BigDecimal) row.get("current_balance"))
                .interestRate((BigDecimal) row.get("interest_rate"))
                .interestRateType((String) row.get("interest_rate_type"))
                .totalInstallments((Integer) row.get("total_installments"))
                .remainingInstallments((Integer) row.get("remaining_installments"))
                .installmentAmount((BigDecimal) row.get("installment_amount"))
                .startDate(row.get("start_date", LocalDate.class))
                .nextPaymentDate(row.get("next_payment_date", LocalDate.class))
                .status((String) row.get("status"))
                .notes((String) row.get("notes"))
                .createdAt(row.get("created_at", LocalDateTime.class))
                .build();
    }
}
