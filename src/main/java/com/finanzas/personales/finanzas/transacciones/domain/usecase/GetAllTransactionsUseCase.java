package com.finanzas.personales.finanzas.transacciones.domain.usecase;

import com.finanzas.personales.finanzas.transacciones.domain.model.TransactionsDto;
import com.finanzas.personales.finanzas.transacciones.domain.model.port.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

/**
 * Caso de uso para obtener todas las transacciones de un usuario sin paginación.
 * Útil para exportaciones, reportes completos o vistas que necesitan todos los datos.
 */
@RequiredArgsConstructor
public class GetAllTransactionsUseCase {

    /** Puerto de salida para acceder al repositorio de transacciones. */
    private final TransactionRepositoryPort transactionRepositoryPort;

    /**
     * Obtiene todas las transacciones de un usuario.
     *
     * @param userId identificador del usuario (extraído del JWT)
     * @return {@code Flux<TransactionsDto>} con todas las transacciones del usuario
     */
    public Flux<TransactionsDto> execute(String userId) {
        return transactionRepositoryPort.findAllByUserId(userId);
    }
}
