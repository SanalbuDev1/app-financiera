package com.finanzas.personales.finanzas.deudas.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Modelo de dominio que representa el resultado del cálculo de amortización.
 * Contiene el valor de la cuota fija y la tabla de amortización completa.
 */
@Data
@Builder
public class AmortizationResult {

    /** Valor de la cuota fija (sistema francés). */
    private BigDecimal installmentAmount;

    /** Total de intereses a pagar durante toda la vida del crédito. */
    private BigDecimal totalInterest;

    /** Total a pagar (principal + intereses). */
    private BigDecimal totalPayment;

    /** Tabla de amortización con todas las cuotas proyectadas. */
    private List<DebtScheduleItem> schedule;
}
