package com.finanzas.personales.finanzas.deudas.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modelo de dominio que representa una deuda del usuario.
 * Contiene la información principal de la deuda incluyendo
 * montos, tasas, plazos y estado actual.
 */
@Data
@Builder
public class Debt {

    /** Identificador único de la deuda (UUID como String). */
    private String id;

    /** Identificador del usuario propietario de la deuda. */
    private String userId;

    /** Identificador del tipo de deuda (tarjeta, préstamo, etc.). */
    private String debtTypeId;

    /** Nombre del tipo de deuda (para mostrar). */
    private String debtTypeName;

    /** Identificador de la frecuencia de pago. */
    private String frequencyId;

    /** Nombre de la frecuencia de pago (mensual, quincenal). */
    private String frequencyName;

    /** Nombre del acreedor (banco, persona, etc.). */
    private String creditor;

    /** Descripción de la deuda. */
    private String description;

    /** Monto original de la deuda. */
    private BigDecimal originalAmount;

    /** Saldo actual de la deuda. */
    private BigDecimal currentBalance;

    /** Tasa de interés (porcentaje). */
    private BigDecimal interestRate;

    /** Tipo de tasa: 'monthly' o 'annual'. */
    private String interestRateType;

    /** Número total de cuotas. */
    private Integer totalInstallments;

    /** Número de cuotas restantes. */
    private Integer remainingInstallments;

    /** Valor de la cuota actual. */
    private BigDecimal installmentAmount;

    /** Fecha de inicio de la deuda. */
    private LocalDate startDate;

    /** Fecha del próximo pago. */
    private LocalDate nextPaymentDate;

    /** Estado de la deuda: 'active', 'paid_off', 'defaulted'. */
    private String status;

    /** Notas adicionales. */
    private String notes;

    /** Fecha de creación del registro. */
    private LocalDateTime createdAt;
}
