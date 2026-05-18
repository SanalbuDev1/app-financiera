package com.finanzas.personales.finanzas.deudas.domain.usecase;

import com.finanzas.personales.finanzas.deudas.domain.model.Debt;
import com.finanzas.personales.finanzas.deudas.domain.model.DebtDetail;
import com.finanzas.personales.finanzas.deudas.domain.model.DebtScheduleItem;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtRepositoryPort;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtScheduleRepositoryPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para obtener el detalle completo de una deuda.
 * Retorna la deuda junto con su tabla de amortización completa,
 * verificando que la deuda pertenezca al usuario autenticado.
 */
@RequiredArgsConstructor
public class GetDebtDetailUseCase {

    private final DebtRepositoryPort debtRepositoryPort;
    private final DebtScheduleRepositoryPort debtScheduleRepositoryPort;

    /**
     * Obtiene una deuda con su tabla de amortización.
     * Falla con {@link IllegalArgumentException} si la deuda no existe
     * o no pertenece al usuario.
     *
     * @param debtId identificador de la deuda
     * @param userId identificador del usuario autenticado
     * @return {@code Mono<DebtDetail>} con la deuda y su cronograma de pagos
     */
    public Mono<DebtDetail> execute(String debtId, String userId) {
        return debtRepositoryPort.findByIdAndUserId(debtId, userId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Deuda no encontrada: " + debtId)))
                .flatMap(debt -> debtScheduleRepositoryPort.findByDebtId(debt.getId())
                        .collectList()
                        .map(schedule -> new DebtDetail(debt, schedule)));
    }
}
