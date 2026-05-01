package com.finanzas.personales.finanzas.transacciones.infrastructure.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entidad R2DBC que representa la tabla maestra {@code categories} en PostgreSQL.
 * Contiene las categorías disponibles para clasificar transacciones.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("categories")
public class CategoryEntity {

    /** Identificador único de la categoría (ej. 'cat-food'). */
    @Id
    private String id;

    /** Nombre corto de la categoría: food, transport, bills, etc. */
    private String name;

    /** Descripción legible de la categoría. */
    private String description;

    /** Nombre del icono para el frontend (Material Icons). */
    private String icon;

    /** Indica si la categoría está activa y disponible para uso. */
    private Boolean active;
}
