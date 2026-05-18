package com.finanzas.personales.finanzas.deudas.domain.usecase;

import com.finanzas.personales.finanzas.deudas.domain.model.Debt;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtRepositoryPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

/**
 * Caso de uso para listar las deudas de un usuario.
 * Permite filtrar opcionalmente por estado (active, paid_off, defaulted).
 * Si no se especifica estado, retorna todas las deudas del usuario.
 */
@RequiredArgsConstructor
public class ListDebtsUseCase {

    private final DebtRepositoryPort debtRepositoryPort;

    /**
     * Obtiene las deudas de un usuario con filtro opcional por estado.
     *
     * @param userId identificador del usuario autenticado
     * @param status estado para filtrar (null o vacío para obtener todas)
     * @return {@code Flux<Debt>} con las deudas encontradas
     */
    public Flux<Debt> execute(String userId, String status) {
        if (status != null && !status.isBlank()) {
            return debtRepositoryPort.findByUserIdAndStatus(userId, status);
        }
        return debtRepositoryPort.findAllByUserId(userId);
    }
}
