package com.finanzas.personales.finanzas.transacciones.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modelo de dominio que representa una transacción financiera (ingreso o gasto).
 * Clase pura sin dependencias de frameworks — solo Java y Lombok.
 * La categoría y tipo se manejan como strings validados contra las tablas maestras en BD.
 */
@Data
@Builder
public class TransactionsDto {

    /** Identificador único de la transacción (UUID). */
    private String id;

    /** Identificador del usuario propietario de la transacción. */
    private String userId;

    /** Descripción breve de la transacción. */
    private String description;

    /** Monto de la transacción. Siempre positivo, con precisión decimal. */
    private BigDecimal amount;

    /** Categoría de clasificación (lowercase). Validada contra tabla categories en BD. */
    private String category;

    /** Tipo de transacción: "income" o "expense". Validado contra tabla transaction_types en BD. */
    private String type;

    /** Fecha en la que se realizó la transacción. */
    private LocalDate transactionDate;

    /** Notas adicionales opcionales. */
    private String notes;

    /** Fecha y hora de creación del registro. */
    private LocalDateTime createdAt;
}
