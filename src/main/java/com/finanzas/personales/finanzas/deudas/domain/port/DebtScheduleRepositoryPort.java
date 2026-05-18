package com.finanzas.personales.finanzas.deudas.domain.port;

import com.finanzas.personales.finanzas.deudas.domain.model.DebtScheduleItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

/**
 * Puerto de salida (output port) del dominio para operaciones de la tabla de amortización.
 * Define el contrato para persistir y consultar los ítems del cronograma de pagos.
 */
public interface DebtScheduleRepositoryPort {

    /**
     * Guarda todos los ítems del cronograma de una deuda.
     *
     * @param items lista de ítems a guardar
     * @return {@code Flux<DebtScheduleItem>} con los ítems guardados
     */
    Flux<DebtScheduleItem> saveAll(List<DebtScheduleItem> items);

    /**
     * Obtiene el cronograma completo de una deuda.
     *
     * @param debtId identificador de la deuda
     * @return {@code Flux<DebtScheduleItem>} con los ítems ordenados por número de cuota
     */
    Flux<DebtScheduleItem> findByDebtId(String debtId);

    /**
     * Obtiene las cuotas pendientes de una deuda.
     *
     * @param debtId identificador de la deuda
     * @return {@code Flux<DebtScheduleItem>} con las cuotas pendientes
     */
    Flux<DebtScheduleItem> findPendingByDebtId(String debtId);

    /**
     * Obtiene la próxima cuota pendiente de una deuda.
     *
     * @param debtId identificador de la deuda
     * @return {@code Mono<DebtScheduleItem>} con la próxima cuota, o vacío si no hay pendientes
     */
    Mono<DebtScheduleItem> findNextPendingByDebtId(String debtId);

    /**
     * Busca cuotas que vencen en o antes de una fecha específica.
     *
     * @param userId  identificador del usuario
     * @param dueDate fecha límite de vencimiento
     * @return {@code Flux<DebtScheduleItem>} con las cuotas próximas a vencer
     */
    Flux<DebtScheduleItem> findUpcomingByUserIdAndDueDate(String userId, LocalDate dueDate);

    /**
     * Actualiza el estado de una cuota.
     *
     * @param id     identificador del ítem
     * @param status nuevo estado (pending, paid, partial, overdue)
     * @return {@code Mono<DebtScheduleItem>} con el ítem actualizado
     */
    Mono<DebtScheduleItem> updateStatus(String id, String status);

    /**
     * Elimina todos los ítems del cronograma de una deuda.
     *
     * @param debtId identificador de la deuda
     * @return {@code Mono<Void>} cuando la operación completa
     */
    Mono<Void> deleteByDebtId(String debtId);

    /**
     * Regenera el cronograma de una deuda (elimina y crea nuevo).
     *
     * @param debtId identificador de la deuda
     * @param items  nuevos ítems del cronograma
     * @return {@code Flux<DebtScheduleItem>} con los nuevos ítems guardados
     */
    Flux<DebtScheduleItem> regenerateSchedule(String debtId, List<DebtScheduleItem> items);
}
