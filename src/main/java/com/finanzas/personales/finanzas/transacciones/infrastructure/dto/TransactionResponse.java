package com.finanzas.personales.finanzas.transacciones.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de salida que representa una transacción en las respuestas de la API.
 * Los campos category y type son strings lowercase (resueltos por JOIN desde tablas maestras).
 * El campo transaction_date se serializa como "date" para el frontend.
 */
@Data
@Builder
public class TransactionResponse {

    /** Identificador único de la transacción (UUID). */
    private String id;

    /** Descripción de la transacción. */
    private String description;

    /** Monto de la transacción. */
    private BigDecimal amount;

    /** Categoría de la transacción (string lowercase: "food", "salary", etc.). */
    private String category;

    /** Tipo de transacción: "income" o "expense". */
    private String type;

    /** Fecha en la que se realizó la transacción. Serializado como "date". */
    @JsonProperty("date")
    private LocalDate transactionDate;

    /** Notas adicionales opcionales. */
    private String notes;

    /** Fecha y hora de creación del registro. */
    private LocalDateTime createdAt;
}
