package com.finanzas.personales.finanzas.deudas.domain.port;

import com.finanzas.personales.finanzas.deudas.domain.model.Debt;
import com.finanzas.personales.finanzas.deudas.domain.model.DebtSummary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida (output port) del dominio para operaciones de persistencia de deudas.
 * Define el contrato que debe cumplir cualquier adaptador de base de datos.
 */
public interface DebtRepositoryPort {

    /**
     * Persiste una nueva deuda.
     *
     * @param debt deuda a guardar
     * @return {@code Mono<Debt>} con la deuda guardada, incluyendo el ID generado
     */
    Mono<Debt> save(Debt debt);

    /**
     * Actualiza una deuda existente.
     *
     * @param debt deuda con los datos actualizados
     * @return {@code Mono<Debt>} con la deuda actualizada
     */
    Mono<Debt> update(Debt debt);

    /**
     * Busca una deuda por su ID.
     *
     * @param id identificador de la deuda
     * @return {@code Mono<Debt>} con la deuda encontrada, o vacío si no existe
     */
    Mono<Debt> findById(String id);

    /**
     * Busca una deuda por ID verificando que pertenezca al usuario.
     *
     * @param id     identificador de la deuda
     * @param userId identificador del usuario
     * @return {@code Mono<Debt>} con la deuda encontrada, o vacío si no existe o no pertenece al usuario
     */
    Mono<Debt> findByIdAndUserId(String id, String userId);

    /**
     * Obtiene todas las deudas de un usuario.
     *
     * @param userId identificador del usuario
     * @return {@code Flux<Debt>} con las deudas del usuario
     */
    Flux<Debt> findAllByUserId(String userId);

    /**
     * Obtiene las deudas activas de un usuario.
     *
     * @param userId identificador del usuario
     * @return {@code Flux<Debt>} con las deudas activas
     */
    Flux<Debt> findActiveByUserId(String userId);

    /**
     * Obtiene las deudas de un usuario filtradas por estado.
     *
     * @param userId identificador del usuario
     * @param status estado de la deuda (active, paid_off, defaulted)
     * @return {@code Flux<Debt>} con las deudas filtradas
     */
    Flux<Debt> findByUserIdAndStatus(String userId, String status);

    /**
     * Obtiene un resumen de las deudas activas del usuario.
     *
     * @param userId identificador del usuario
     * @return {@code Mono<DebtSummary>} con el resumen agregado
     */
    Mono<DebtSummary> getSummaryByUserId(String userId);

    /**
     * Elimina una deuda por su ID.
     *
     * @param id identificador de la deuda
     * @return {@code Mono<Void>} cuando la operación completa
     */
    Mono<Void> deleteById(String id);

    /**
     * Verifica si existe una deuda con el ID dado.
     *
     * @param id identificador de la deuda
     * @return {@code Mono<Boolean>} true si existe, false si no
     */
    Mono<Boolean> existsById(String id);
}
