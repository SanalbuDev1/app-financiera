package com.finanzas.personales.finanzas.deudas.infrastructure.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de entrada para registrar un pago sobre una deuda.
 * Usado en {@code POST /api/debts/{id}/payments}.
 */
@Data
public class DebtPaymentRequest {

    /** Fecha del pago. Si no se envía, el controlador usa la fecha actual. */
    private LocalDate paymentDate;

    @NotNull(message = "El monto total del pago es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    private BigDecimal totalAmount;

    /** Tipo de pago: 'regular' o 'extra'. */
    @NotBlank(message = "El tipo de pago es obligatorio")
    private String paymentType;

    /**
     * Estrategia para abonos extraordinarios: 'reduce_installment' o 'reduce_term'.
     * Solo aplica cuando {@code paymentType} es 'extra'.
     */
    private String extraPaymentStrategy;

    private String notes;
}
