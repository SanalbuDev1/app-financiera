package com.finanzas.personales.finanzas.deudas.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Modelo de dominio que representa un resumen de las deudas del usuario.
 * Contiene totales agregados para mostrar en el dashboard.
 */
@Data
@Builder
public class DebtSummary {

    /** Número total de deudas activas. */
    private Integer totalDebts;

    /** Suma del saldo actual de todas las deudas activas. */
    private BigDecimal totalBalance;

    /** Suma del monto original de todas las deudas activas. */
    private BigDecimal totalOriginalAmount;

    /** Suma de todas las cuotas mensuales/quincenales. */
    private BigDecimal totalMonthlyPayment;

    /** Total de intereses pendientes por pagar. */
    private BigDecimal totalPendingInterest;

    /** Porcentaje de avance promedio (capital pagado / capital original). */
    private BigDecimal averageProgress;
}
