package com.finanzas.personales.finanzas.deudas.infrastructure.adapter.out;

import com.finanzas.personales.finanzas.config.SqlQueryLoader;
import com.finanzas.personales.finanzas.deudas.domain.model.DebtPayment;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtPaymentRepositoryPort;
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
 * Adaptador de salida que implementa {@link DebtPaymentRepositoryPort} usando R2DBC.
 * Gestiona la persistencia del historial de pagos de deudas.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class R2dbcDebtPaymentAdapter implements DebtPaymentRepositoryPort {

    private final DatabaseClient databaseClient;
    private final SqlQueryLoader sqlQueryLoader;

    /**
     * Registra un nuevo pago de deuda en la base de datos.
     *
     * @param payment pago a guardar
     * @return {@code Mono<DebtPayment>} con el pago guardado
     */
    @Override
    public Mono<DebtPayment> save(DebtPayment payment) {
        log.info("[R2DBC] Registrando pago para deuda id: {}, monto: {}", payment.getDebtId(), payment.getTotalAmount());
        String sql = sqlQueryLoader.load("deudas/registrar_pago");
        // Construir spec paso a paso para poder usar bindNull en extraPaymentStrategy
        var spec = databaseClient.sql(sql)
                .bind("id", payment.getId())
                .bind("debtId", payment.getDebtId())
                .bind("paymentDate", payment.getPaymentDate())
                .bind("totalAmount", payment.getTotalAmount())
                .bind("principalAmount", payment.getPrincipalAmount())
                .bind("interestAmount", payment.getInterestAmount())
                .bind("paymentType", payment.getPaymentType());

        // extraPaymentStrategy es NULL para pagos regulares; el constraint solo acepta los dos valores o NULL
        if (payment.getExtraPaymentStrategy() != null) {
            spec = spec.bind("extraPaymentStrategy", payment.getExtraPaymentStrategy());
        } else {
            spec = spec.bindNull("extraPaymentStrategy", String.class);
        }

        return spec
                .bind("notes", payment.getNotes() != null ? payment.getNotes() : "")
                .bind("createdAt", payment.getCreatedAt() != null ? payment.getCreatedAt() : LocalDateTime.now())
                .fetch()
                .rowsUpdated()
                .thenReturn(payment);  // retornamos el objeto ya construido, evitando SELECT innecesario
    }

    /**
     * Obtiene el historial de pagos de una deuda ordenados por fecha descendente.
     *
     * @param debtId identificador de la deuda
     * @return {@code Flux<DebtPayment>} con los pagos
     */
    @Override
    public Flux<DebtPayment> findByDebtId(String debtId) {
        log.debug("[R2DBC] Listando pagos para deuda id: {}", debtId);
        String sql = sqlQueryLoader.load("deudas/listar_pagos_por_deuda");
        return databaseClient.sql(sql)
                .bind("debtId", debtId)
                .map(this::mapRowToDomain)
                .all();
    }

    /**
     * Busca un pago por su ID.
     *
     * @param id identificador del pago
     * @return {@code Mono<DebtPayment>} con el pago, o vacío si no existe
     */
    @Override
    public Mono<DebtPayment> findById(String id) {
        log.debug("[R2DBC] Buscando pago por id: {}", id);
        // Reutilizamos listar_pagos_por_deuda con un filtro directo via SQL dinámico
        // ya que no existe un archivo independiente por ID de pago
        return databaseClient.sql("SELECT * FROM debt_payments WHERE id = :id")
                .bind("id", id)
                .map(this::mapRowToDomain)
                .one();
    }

    /**
     * Calcula el total pagado acumulado a una deuda.
     *
     * @param debtId identificador de la deuda
     * @return {@code Mono<BigDecimal>} con el total pagado
     */
    @Override
    public Mono<BigDecimal> getTotalPaidByDebtId(String debtId) {
        return databaseClient.sql(
                        "SELECT COALESCE(SUM(total_amount), 0) AS total FROM debt_payments WHERE debt_id = :debtId")
                .bind("debtId", debtId)
                .map(row -> (BigDecimal) row.get("total"))
                .one()
                .defaultIfEmpty(BigDecimal.ZERO);
    }

    /**
     * Calcula el total de capital pagado a una deuda.
     *
     * @param debtId identificador de la deuda
     * @return {@code Mono<BigDecimal>} con el total de capital pagado
     */
    @Override
    public Mono<BigDecimal> getTotalPrincipalPaidByDebtId(String debtId) {
        return databaseClient.sql(
                        "SELECT COALESCE(SUM(principal_amount), 0) AS total FROM debt_payments WHERE debt_id = :debtId")
                .bind("debtId", debtId)
                .map(row -> (BigDecimal) row.get("total"))
                .one()
                .defaultIfEmpty(BigDecimal.ZERO);
    }

    /**
     * Calcula el total de intereses pagados a una deuda.
     *
     * @param debtId identificador de la deuda
     * @return {@code Mono<BigDecimal>} con el total de intereses pagados
     */
    @Override
    public Mono<BigDecimal> getTotalInterestPaidByDebtId(String debtId) {
        return databaseClient.sql(
                        "SELECT COALESCE(SUM(interest_amount), 0) AS total FROM debt_payments WHERE debt_id = :debtId")
                .bind("debtId", debtId)
                .map(row -> (BigDecimal) row.get("total"))
                .one()
                .defaultIfEmpty(BigDecimal.ZERO);
    }

    /**
     * Mapea una fila del ResultSet al modelo de dominio {@link DebtPayment}.
     *
     * @param row fila del resultado SQL
     * @return objeto de dominio {@link DebtPayment}
     */
    /**
     * Cuenta el número de pagos de una deuda.
     *
     * @param debtId identificador de la deuda
     * @return {@code Mono<Long>} con el total de pagos
     */
    @Override
    public Mono<Long> countByDebtId(String debtId) {
        return databaseClient.sql(
                        "SELECT COUNT(*) AS cnt FROM debt_payments WHERE debt_id = :debtId")
                .bind("debtId", debtId)
                .map((row, m) -> ((Number) row.get("cnt")).longValue())
                .one()
                .defaultIfEmpty(0L);
    }

    /**
     * Elimina todos los pagos de una deuda.
     *
     * @param debtId identificador de la deuda
     * @return {@code Mono<Void>} cuando la operación completa
     */
    @Override
    public Mono<Void> deleteByDebtId(String debtId) {
        log.info("[R2DBC] Eliminando pagos de deuda id: {}", debtId);
        return databaseClient.sql("DELETE FROM debt_payments WHERE debt_id = :debtId")
                .bind("debtId", debtId)
                .fetch()
                .rowsUpdated()
                .then();
    }

    private DebtPayment mapRowToDomain(Row row, RowMetadata metadata) {
        return DebtPayment.builder()
                .id((String) row.get("id"))
                .debtId((String) row.get("debt_id"))
                .paymentDate(row.get("payment_date", LocalDate.class))
                .totalAmount((BigDecimal) row.get("total_amount"))
                .principalAmount((BigDecimal) row.get("principal_amount"))
                .interestAmount((BigDecimal) row.get("interest_amount"))
                .paymentType((String) row.get("payment_type"))
                .extraPaymentStrategy((String) row.get("extra_payment_strategy"))
                .notes((String) row.get("notes"))
                .createdAt(row.get("created_at", LocalDateTime.class))
                .build();
    }
}
