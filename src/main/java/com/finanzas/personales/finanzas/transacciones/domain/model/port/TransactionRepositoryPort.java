package com.finanzas.personales.finanzas.transacciones.domain.model.port;

import com.finanzas.personales.finanzas.transacciones.domain.model.TransactionsDto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Puerto de salida (output port) del dominio para operaciones de persistencia de transacciones.
 * Define el contrato que debe cumplir cualquier adaptador de base de datos.
 * La capa de dominio depende de esta interfaz; la infraestructura la implementa.
 */
public interface TransactionRepositoryPort {

    /**
     * Persiste una nueva transacción en el almacenamiento.
     *
     * @param transaction transacción a guardar
     * @return {@code Mono<TransactionsDto>} con la transacción guardada
     */
    Mono<TransactionsDto> save(TransactionsDto transaction);

    /**
     * Busca una transacción por su identificador único.
     *
     * @param transactionId UUID de la transacción
     * @return {@code Mono<TransactionsDto>} con la transacción encontrada, o vacío si no existe
     */
    Mono<TransactionsDto> findById(String transactionId);

    /**
     * Obtiene todas las transacciones de un usuario sin paginación.
     *
     * @param userId identificador del usuario propietario
     * @return {@code Flux<TransactionsDto>} con todas las transacciones del usuario
     */
    Flux<TransactionsDto> findAllByUserId(String userId);

    /**
     * Obtiene transacciones paginadas con filtros opcionales.
     * Resuelve JOINs con tablas maestras para devolver nombres de categoría y tipo.
     *
     * @param userId   identificador del usuario propietario
     * @param from     fecha inicio (inclusive), puede ser null
     * @param to       fecha fin (inclusive), puede ser null
     * @param type     nombre del tipo ("income"/"expense"), puede ser null
     * @param category nombre de la categoría, puede ser null
     * @param offset   desplazamiento para paginación
     * @param limit    cantidad de resultados por página
     * @return {@code Flux<TransactionsDto>} con las transacciones paginadas
     */
    Flux<TransactionsDto> findByUserIdPaginated(String userId, LocalDate from, LocalDate to,
                                                 String type, String category, int offset, int limit);

    /**
     * Cuenta el total de transacciones que coinciden con los filtros.
     *
     * @param userId   identificador del usuario
     * @param from     fecha inicio (inclusive), puede ser null
     * @param to       fecha fin (inclusive), puede ser null
     * @param type     nombre del tipo, puede ser null
     * @param category nombre de la categoría, puede ser null
     * @return {@code Mono<Long>} con el total de transacciones
     */
    Mono<Long> countByUserIdFiltered(String userId, LocalDate from, LocalDate to,
                                      String type, String category);

    /**
     * Calcula el balance total histórico del usuario (suma ingresos - suma gastos).
     *
     * @param userId identificador del usuario
     * @return {@code Mono<BigDecimal>} con el balance total
     */
    Mono<BigDecimal> getTotalBalance(String userId);

    /**
     * Calcula la suma de ingresos de un mes/año específico.
     *
     * @param userId identificador del usuario
     * @param month  mes (1-12)
     * @param year   año
     * @return {@code Mono<BigDecimal>} con la suma de ingresos
     */
    Mono<BigDecimal> getMonthlyIncome(String userId, int month, int year);

    /**
     * Calcula la suma de gastos de un mes/año específico.
     *
     * @param userId identificador del usuario
     * @param month  mes (1-12)
     * @param year   año
     * @return {@code Mono<BigDecimal>} con la suma de gastos
     */
    Mono<BigDecimal> getMonthlyExpenses(String userId, int month, int year);

    /**
     * Elimina una transacción que pertenece a un usuario específico.
     * Solo elimina si el user_id coincide con el dueño.
     *
     * @param transactionId UUID de la transacción a eliminar
     * @param userId        UUID del usuario dueño
     * @return {@code Mono<Long>} con la cantidad de filas eliminadas (0 o 1)
     */
    Mono<Long> deleteByIdAndUserId(String transactionId, String userId);

    /**
     * Verifica si existe una transacción con el ID dado.
     *
     * @param transactionId UUID de la transacción
     * @return {@code Mono<Boolean>} true si existe, false si no
     */
    Mono<Boolean> existsById(String transactionId);

    /**
     * Actualiza una transacción existente que pertenece al usuario indicado.
     *
     * @param transaction transacción con los datos actualizados
     * @return {@code Mono<Long>} con la cantidad de filas actualizadas (0 o 1)
     */
    Mono<Long> update(TransactionsDto transaction);
}
