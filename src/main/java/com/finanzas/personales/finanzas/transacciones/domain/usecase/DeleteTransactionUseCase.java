package com.finanzas.personales.finanzas.transacciones.domain.usecase;

import com.finanzas.personales.finanzas.transacciones.domain.model.port.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para eliminar una transacción.
 * Verifica que la transacción pertenezca al usuario antes de eliminarla.
 * No tiene anotaciones de Spring — se registra como @Bean en ApplicationConfig.
 */
@RequiredArgsConstructor
public class DeleteTransactionUseCase {

    /** Puerto de salida para operaciones de persistencia. */
    private final TransactionRepositoryPort transactionRepositoryPort;

    /**
     * Ejecuta la eliminación de una transacción.
     * Solo elimina si el user_id del JWT coincide con el dueño de la transacción.
     *
     * @param transactionId UUID de la transacción a eliminar
     * @param userId        UUID del usuario (del JWT)
     * @return {@code Mono<Long>} con filas eliminadas (1 si OK, 0 si no existe o no es del usuario)
     */
    public Mono<Long> execute(String transactionId, String userId) {
        return transactionRepositoryPort.deleteByIdAndUserId(transactionId, userId);
    }
}
