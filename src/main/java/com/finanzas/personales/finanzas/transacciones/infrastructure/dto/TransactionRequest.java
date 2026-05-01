package com.finanzas.personales.finanzas.transacciones.infrastructure.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de entrada para la creación de una transacción.
 * Recibe los datos desde el frontend Angular y se valida con Bean Validation.
 * Los campos category y type son strings que deben coincidir con las tablas maestras.
 */
@Data
public class TransactionRequest {

    /** Descripción de la transacción. Obligatoria, máximo 255 caracteres. */
    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String description;

    /** Monto de la transacción. Debe ser mayor que cero. */
    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor que cero")
    private BigDecimal amount;

    /** Categoría (string lowercase: "food", "salary", etc.). Debe existir en tabla categories. */
    @NotBlank(message = "La categoría es obligatoria")
    private String category;

    /** Tipo de transacción: "income" o "expense". Debe existir en tabla transaction_types. */
    @NotBlank(message = "El tipo de transacción es obligatorio")
    private String type;

    /** Fecha en la que se realizó la transacción. */
    @NotNull(message = "La fecha de la transacción es obligatoria")
    private LocalDate transactionDate;

    /** Notas adicionales opcionales. Máximo 500 caracteres. */
    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notes;
}
