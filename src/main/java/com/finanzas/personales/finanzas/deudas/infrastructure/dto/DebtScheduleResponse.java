package com.finanzas.personales.finanzas.deudas.infrastructure.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de salida que representa una cuota en la tabla de amortización.
 * Usado en respuestas de {@code GET /api/debts/{id}/schedule}.
 */
@Data
@Builder
public class DebtScheduleResponse {

    private String id;
    private String debtId;
    private Integer installmentNumber;
    private LocalDate dueDate;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal totalAmount;
    private BigDecimal balanceAfter;

    /** Estado de la cuota: 'pending', 'paid', 'partial', 'overdue'. */
    private String status;

    private LocalDateTime createdAt;
}
