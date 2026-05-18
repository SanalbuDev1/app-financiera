package com.finanzas.personales.finanzas.deudas.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Comando de entrada para el caso de uso {@code CreateDebtUseCase}.
 * Contiene todos los datos necesarios para crear una nueva deuda
 * y generar su tabla de amortización.
 */
@Data
@Builder
public class CreateDebtCommand {
    /** Identificador del usuario propietario de la deuda. */
    private String userId;
    /** Identificador del tipo de deuda. */
    private String debtTypeId;
    /** Identificador de la frecuencia de pago. */
    private String frequencyId;
    /** Nombre del acreedor (banco, persona, etc.). */
    private String creditor;
    /** Descripción de la deuda. */
    private String description;
    /** Monto original del préstamo. */
    private BigDecimal originalAmount;
    /** Tasa de interés en porcentaje (ej: 1.5 para 1.5%). */
    private BigDecimal interestRate;
    /** Tipo de tasa: 'monthly' o 'annual'. */
    private String interestRateType;
    /** Número total de cuotas. */
    private Integer totalInstallments;
    /** Fecha de inicio del préstamo (primera cuota se calcula desde aquí). */
    private LocalDate startDate;
    /** Notas adicionales (opcional). */
    private String notes;
}
