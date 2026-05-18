package com.finanzas.personales.finanzas.deudas.domain.model;

import lombok.Builder;
import lombok.Data;

/**
 * Comando de entrada para el caso de uso {@code UpdateDebtUseCase}.
 * Solo contiene los campos editables de una deuda.
 * Los datos financieros (montos, tasas, cuotas) no son modificables directamente.
 * Los campos null se ignoran — no sobreescriben el valor actual.
 */
@Data
@Builder
public class UpdateDebtCommand {
    /** Identificador de la deuda a actualizar. Obligatorio. */
    private String debtId;
    /** Identificador del usuario propietario. Obligatorio. */
    private String userId;
    /** Nuevo nombre del acreedor (null = sin cambio). */
    private String creditor;
    /** Nueva descripción (null = sin cambio). */
    private String description;
    /** Nuevas notas (null = sin cambio). */
    private String notes;
}
