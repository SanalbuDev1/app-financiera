package com.finanzas.personales.finanzas.deudas.infrastructure.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de salida que representa un pago registrado sobre una deuda.
 * Usado en respuestas de {@code POST /api/debts/{id}/payments}
 * y {@code GET /api/debts/{id}/payments}.
 */
@Data
@Builder
public class DebtPaymentResponse {

    private String id;
    private String debtId;
    private LocalDate paymentDate;
    private BigDecimal totalAmount;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private String paymentType;
    private String extraPaymentStrategy;
    private String notes;
    private LocalDateTime createdAt;
}
