package com.finanzas.personales.finanzas.deudas.domain.port;

import com.finanzas.personales.finanzas.deudas.domain.model.PaymentFrequency;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida (output port) del dominio para operaciones del catálogo de frecuencias de pago.
 * Define el contrato para consultar las frecuencias de pago disponibles.
 */
public interface PaymentFrequencyRepositoryPort {

    /**
     * Obtiene todas las frecuencias de pago disponibles.
     *
     * @return {@code Flux<PaymentFrequency>} con las frecuencias
     */
    Flux<PaymentFrequency> findAll();

    /**
     * Busca una frecuencia por su ID.
     *
     * @param id identificador de la frecuencia
     * @return {@code Mono<PaymentFrequency>} con la frecuencia, o vacío si no existe
     */
    Mono<PaymentFrequency> findById(String id);

    /**
     * Busca una frecuencia por su nombre.
     *
     * @param name nombre de la frecuencia (mensual, quincenal)
     * @return {@code Mono<PaymentFrequency>} con la frecuencia, o vacío si no existe
     */
    Mono<PaymentFrequency> findByName(String name);
}
