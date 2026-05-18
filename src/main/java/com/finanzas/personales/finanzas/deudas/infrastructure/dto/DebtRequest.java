package com.finanzas.personales.finanzas.deudas.infrastructure.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de entrada para crear o actualizar una deuda.
 * Usado en {@code POST /api/debts} y {@code PUT /api/debts/{id}}.
 */
@Data
public class DebtRequest {

    @NotBlank(message = "El acreedor es obligatorio")
    private String creditor;

    @NotBlank(message = "La descripción es obligatoria")
    private String description;

    @NotBlank(message = "El tipo de deuda es obligatorio")
    private String debtTypeId;

    @NotBlank(message = "La frecuencia de pago es obligatoria")
    private String frequencyId;

    @NotNull(message = "El monto original es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    private BigDecimal originalAmount;

    @NotNull(message = "La tasa de interés es obligatoria")
    @DecimalMin(value = "0.0", inclusive = true, message = "La tasa no puede ser negativa")
    private BigDecimal interestRate;

    /** Tipo de tasa: 'monthly' o 'annual'. */
    @NotBlank(message = "El tipo de tasa es obligatorio")
    private String interestRateType;

    @NotNull(message = "El número de cuotas es obligatorio")
    @Min(value = 1, message = "Debe haber al menos 1 cuota")
    private Integer totalInstallments;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate startDate;

    /** Notas adicionales opcionales. */
    private String notes;
}
