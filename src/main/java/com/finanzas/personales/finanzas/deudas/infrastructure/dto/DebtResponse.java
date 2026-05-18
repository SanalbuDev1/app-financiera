package com.finanzas.personales.finanzas.deudas.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de salida que representa una deuda en las respuestas HTTP.
 * Usado en todas las respuestas de {@code GET /api/debts} y operaciones CRUD.
 */
@Data
@Builder
public class DebtResponse {

    private String id;
    private String userId;
    private String debtTypeId;
    private String debtTypeName;
    private String frequencyId;
    private String frequencyName;
    private String creditor;
    private String description;
    private BigDecimal originalAmount;
    private BigDecimal currentBalance;
    private BigDecimal interestRate;
    private String interestRateType;
    private Integer totalInstallments;
    private Integer remainingInstallments;
    private BigDecimal installmentAmount;
    private LocalDate startDate;
    private LocalDate nextPaymentDate;
    private String status;
    private String notes;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    /** Porcentaje de progreso: (1 - currentBalance/originalAmount) * 100. */
    private BigDecimal progressPercentage;
}
