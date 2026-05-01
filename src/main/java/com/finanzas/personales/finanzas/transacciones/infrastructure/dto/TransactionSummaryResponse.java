package com.finanzas.personales.finanzas.transacciones.infrastructure.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO de salida para el resumen financiero del usuario.
 * Contiene balance total, ingresos/gastos del mes y meta de ahorro.
 */
@Data
@Builder
public class TransactionSummaryResponse {

    /** Balance total histórico del usuario (ingresos - gastos). */
    private BigDecimal totalBalance;

    /** Total de ingresos del mes consultado. */
    private BigDecimal monthlyIncome;

    /** Total de gastos del mes consultado. */
    private BigDecimal monthlyExpenses;

    /** Ahorro del mes (monthlyIncome - monthlyExpenses). */
    private BigDecimal monthlySavings;

    /** Meta de ahorro mensual (configurable, default 3000). */
    private BigDecimal savingsGoal;
}
