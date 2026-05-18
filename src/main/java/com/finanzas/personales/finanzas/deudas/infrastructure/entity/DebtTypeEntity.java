package com.finanzas.personales.finanzas.deudas.infrastructure.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entidad R2DBC que representa la tabla maestra {@code debt_types} en PostgreSQL.
 * Contiene los tipos de deuda disponibles (tarjeta, préstamo bancario, etc.).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("debt_types")
public class DebtTypeEntity {

    @Id
    private String id;

    private String name;
    private String description;
    private String icon;
    private Boolean active;
}
