package com.finanzas.personales.finanzas.deudas.infrastructure.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO de salida con el resumen financiero de las deudas del usuario.
 * Usado en la respuesta de {@code GET /api/debts/summary}.
 */
@Data
@Builder
public class DebtSummaryResponse {

    /** Número total de deudas activas. */
    private Integer totalDebts;

    /** Suma del saldo actual de todas las deudas activas. */
    private BigDecimal totalBalance;

    /** Suma del monto original de todas las deudas activas. */
    private BigDecimal totalOriginalAmount;

    /** Suma de las cuotas mensuales/quincenales de todas las deudas activas. */
    private BigDecimal totalMonthlyPayment;

    /** Total de intereses pendientes por pagar. */
    private BigDecimal totalPendingInterest;

    /** Porcentaje de avance promedio (capital pagado / capital original). */
    private BigDecimal averageProgress;
}
