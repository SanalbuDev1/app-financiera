package com.finanzas.personales.finanzas.transacciones.infrastructure.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO genérico de respuesta paginada.
 * Envuelve una lista de contenido con metadatos de paginación.
 *
 * @param <T> tipo de los elementos en la página
 */
@Data
@Builder
public class TransactionPageResponse<T> {

    /** Lista de elementos de la página actual. */
    private List<T> content;

    /** Número total de elementos que coinciden con los filtros. */
    private long totalElements;

    /** Número total de páginas. */
    private int totalPages;

    /** Número de la página actual (0-indexed). */
    private int page;

    /** Tamaño de la página. */
    private int size;
}
