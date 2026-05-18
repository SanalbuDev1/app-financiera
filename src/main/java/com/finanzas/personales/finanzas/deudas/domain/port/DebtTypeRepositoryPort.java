package com.finanzas.personales.finanzas.deudas.domain.port;

import com.finanzas.personales.finanzas.deudas.domain.model.DebtType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida (output port) del dominio para operaciones del catálogo de tipos de deuda.
 * Define el contrato para consultar y administrar los tipos de deuda.
 */
public interface DebtTypeRepositoryPort {

    /**
     * Obtiene todos los tipos de deuda activos.
     *
     * @return {@code Flux<DebtType>} con los tipos activos
     */
    Flux<DebtType> findAllActive();

    /**
     * Obtiene todos los tipos de deuda (activos e inactivos).
     *
     * @return {@code Flux<DebtType>} con todos los tipos
     */
    Flux<DebtType> findAll();

    /**
     * Busca un tipo de deuda por su ID.
     *
     * @param id identificador del tipo
     * @return {@code Mono<DebtType>} con el tipo, o vacío si no existe
     */
    Mono<DebtType> findById(String id);

    /**
     * Busca un tipo de deuda por su nombre.
     *
     * @param name nombre del tipo (tarjeta_credito, prestamo_bancario, etc.)
     * @return {@code Mono<DebtType>} con el tipo, o vacío si no existe
     */
    Mono<DebtType> findByName(String name);

    /**
     * Guarda un nuevo tipo de deuda (solo admin).
     *
     * @param debtType tipo a guardar
     * @return {@code Mono<DebtType>} con el tipo guardado
     */
    Mono<DebtType> save(DebtType debtType);

    /**
     * Actualiza un tipo de deuda existente (solo admin).
     *
     * @param debtType tipo con los datos actualizados
     * @return {@code Mono<DebtType>} con el tipo actualizado
     */
    Mono<DebtType> update(DebtType debtType);

    /**
     * Desactiva un tipo de deuda (soft delete).
     *
     * @param id identificador del tipo
     * @return {@code Mono<Void>} cuando la operación completa
     */
    Mono<Void> deactivate(String id);

    /**
     * Verifica si existe un tipo con el nombre dado.
     *
     * @param name nombre a verificar
     * @return {@code Mono<Boolean>} true si existe, false si no
     */
    Mono<Boolean> existsByName(String name);
}
