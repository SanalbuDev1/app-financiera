package com.finanzas.personales.finanzas.deudas.domain.usecase;

import com.finanzas.personales.finanzas.deudas.domain.port.DebtRepositoryPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para eliminar una deuda.
 * Verifica que la deuda pertenezca al usuario autenticado antes de eliminar.
 * Los pagos (debt_payments) y la tabla de amortización (debt_schedule)
 * se eliminan automáticamente en cascada por el FK constraint en la base de datos.
 */
@RequiredArgsConstructor
public class DeleteDebtUseCase {

    private final DebtRepositoryPort debtRepositoryPort;

    /**
     * Elimina una deuda verificando la propiedad del usuario.
     * Falla con {@link IllegalArgumentException} si la deuda no existe
     * o no pertenece al usuario.
     *
     * @param debtId identificador de la deuda a eliminar
     * @param userId identificador del usuario autenticado
     * @return {@code Mono<Void>} que completa cuando la deuda es eliminada
     */
    public Mono<Void> execute(String debtId, String userId) {
        return debtRepositoryPort.findByIdAndUserId(debtId, userId)
                .switchIfEmpty(Mono.error(
                        new IllegalArgumentException("Deuda no encontrada: " + debtId)))
                .flatMap(debt -> debtRepositoryPort.deleteById(debt.getId()));
    }
}
