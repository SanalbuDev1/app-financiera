package com.finanzas.personales.finanzas.deudas.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modelo de dominio que representa un ítem de la tabla de amortización.
 * Cada ítem corresponde a una cuota proyectada de la deuda.
 */
@Data
@Builder
public class DebtScheduleItem {

    /** Identificador único del ítem (UUID como String). */
    private String id;

    /** Identificador de la deuda a la que pertenece. */
    private String debtId;

    /** Número de cuota (1, 2, 3, ...). */
    private Integer installmentNumber;

    /** Fecha de vencimiento de la cuota. */
    private LocalDate dueDate;

    /** Monto de capital (abono al principal). */
    private BigDecimal principalAmount;

    /** Monto de intereses. */
    private BigDecimal interestAmount;

    /** Monto total de la cuota (capital + intereses). */
    private BigDecimal totalAmount;

    /** Saldo después de pagar esta cuota. */
    private BigDecimal balanceAfter;

    /** Estado de la cuota: 'pending', 'paid', 'partial', 'overdue'. */
    private String status;

    /** Fecha de creación del registro. */
    private LocalDateTime createdAt;
}
