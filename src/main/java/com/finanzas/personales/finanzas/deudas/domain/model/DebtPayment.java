package com.finanzas.personales.finanzas.deudas.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modelo de dominio que representa un pago realizado a una deuda.
 * Registra tanto pagos regulares como pagos extraordinarios.
 */
@Data
@Builder
public class DebtPayment {

    /** Identificador único del pago (UUID como String). */
    private String id;

    /** Identificador de la deuda a la que se aplica el pago. */
    private String debtId;

    /** Fecha en que se realizó el pago. */
    private LocalDate paymentDate;

    /** Monto total pagado. */
    private BigDecimal totalAmount;

    /** Monto aplicado al capital (principal). */
    private BigDecimal principalAmount;

    /** Monto aplicado a intereses. */
    private BigDecimal interestAmount;

    /** Tipo de pago: 'regular' o 'extra'. */
    private String paymentType;

    /**
     * Estrategia para pagos extraordinarios: 'reduce_installment' o 'reduce_term'.
     * Solo aplica cuando paymentType = 'extra'.
     */
    private String extraPaymentStrategy;

    /** Notas adicionales sobre el pago. */
    private String notes;

    /** Fecha de creación del registro. */
    private LocalDateTime createdAt;
}
