package com.finanzas.personales.finanzas.transacciones.infrastructure.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entidad R2DBC que representa la tabla maestra {@code transaction_types} en PostgreSQL.
 * Contiene los tipos de transacción disponibles: income y expense.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("transaction_types")
public class TransactionTypeEntity {

    /** Identificador único del tipo (ej. 'type-income'). */
    @Id
    private String id;

    /** Nombre del tipo: 'income' o 'expense'. */
    private String name;

    /** Descripción legible del tipo de transacción. */
    private String description;
}
