package com.finanzas.personales.finanzas.deudas.domain.usecase;

import com.finanzas.personales.finanzas.deudas.domain.model.DebtSummary;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtRepositoryPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para obtener el resumen financiero de las deudas activas del usuario.
 * Delega el cálculo al puerto de repositorio, que consolida todos los totales
 * en una única consulta SQL optimizada.
 *
 * <p>El resumen incluye: número de deudas activas, saldo total, monto original total,
 * cuota mensual total, intereses pendientes y porcentaje de avance promedio.</p>
 */
@RequiredArgsConstructor
public class GetDebtSummaryUseCase {

    private final DebtRepositoryPort debtRepositoryPort;

    /**
     * Obtiene el resumen de las deudas activas del usuario.
     * Si el usuario no tiene deudas, retorna un resumen con todos los valores en cero.
     *
     * @param userId identificador del usuario autenticado
     * @return {@code Mono<DebtSummary>} con los totales consolidados
     */
    public Mono<DebtSummary> execute(String userId) {
        return debtRepositoryPort.getSummaryByUserId(userId);
    }
}
