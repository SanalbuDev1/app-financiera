package com.finanzas.personales.finanzas.deudas.domain.port;

import com.finanzas.personales.finanzas.deudas.domain.model.DebtPayment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Puerto de salida (output port) del dominio para operaciones de pagos de deudas.
 * Define el contrato para persistir y consultar el historial de pagos.
 */
public interface DebtPaymentRepositoryPort {

    /**
     * Registra un nuevo pago.
     *
     * @param payment pago a guardar
     * @return {@code Mono<DebtPayment>} con el pago guardado
     */
    Mono<DebtPayment> save(DebtPayment payment);

    /**
     * Obtiene el historial de pagos de una deuda.
     *
     * @param debtId identificador de la deuda
     * @return {@code Flux<DebtPayment>} con los pagos ordenados por fecha descendente
     */
    Flux<DebtPayment> findByDebtId(String debtId);

    /**
     * Obtiene un pago por su ID.
     *
     * @param id identificador del pago
     * @return {@code Mono<DebtPayment>} con el pago, o vacío si no existe
     */
    Mono<DebtPayment> findById(String id);

    /**
     * Calcula el total pagado a una deuda.
     *
     * @param debtId identificador de la deuda
     * @return {@code Mono<BigDecimal>} con el total pagado
     */
    Mono<BigDecimal> getTotalPaidByDebtId(String debtId);

    /**
     * Calcula el total de capital pagado a una deuda.
     *
     * @param debtId identificador de la deuda
     * @return {@code Mono<BigDecimal>} con el total de capital pagado
     */
    Mono<BigDecimal> getTotalPrincipalPaidByDebtId(String debtId);

    /**
     * Calcula el total de intereses pagados a una deuda.
     *
     * @param debtId identificador de la deuda
     * @return {@code Mono<BigDecimal>} con el total de intereses pagados
     */
    Mono<BigDecimal> getTotalInterestPaidByDebtId(String debtId);

    /**
     * Cuenta el número de pagos realizados a una deuda.
     *
     * @param debtId identificador de la deuda
     * @return {@code Mono<Long>} con el número de pagos
     */
    Mono<Long> countByDebtId(String debtId);

    /**
     * Elimina todos los pagos de una deuda.
     *
     * @param debtId identificador de la deuda
     * @return {@code Mono<Void>} cuando la operación completa
     */
    Mono<Void> deleteByDebtId(String debtId);
}
