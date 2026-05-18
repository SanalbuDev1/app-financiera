package com.finanzas.personales.finanzas.deudas.domain.usecase;

import com.finanzas.personales.finanzas.deudas.domain.model.Debt;
import com.finanzas.personales.finanzas.deudas.domain.model.UpdateDebtCommand;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtRepositoryPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para actualizar los campos editables de una deuda.
 * Solo permite modificar descripción, acreedor y notas — los datos
 * financieros (montos, tasa, cuotas) no se pueden cambiar directamente.
 * Verifica que la deuda pertenezca al usuario antes de actualizar.
 */
@RequiredArgsConstructor
public class UpdateDebtUseCase {

    private final DebtRepositoryPort debtRepositoryPort;

    /**
     * Actualiza los campos editables de una deuda.
     * Los campos null en el comando se ignoran (no se sobreescriben).
     *
     * @param command datos de actualización con debtId y userId obligatorios
     * @return {@code Mono<Debt>} con la deuda actualizada
     */
    public Mono<Debt> execute(UpdateDebtCommand command) {
        return debtRepositoryPort.findByIdAndUserId(command.getDebtId(), command.getUserId())
                .switchIfEmpty(Mono.error(
                        new IllegalArgumentException("Deuda no encontrada: " + command.getDebtId())))
                .flatMap(debt -> {
                    // Actualizar solo los campos provistos en el comando (non-null)
                    if (command.getCreditor() != null && !command.getCreditor().isBlank()) {
                        debt.setCreditor(command.getCreditor());
                    }
                    if (command.getDescription() != null) {
                        debt.setDescription(command.getDescription());
                    }
                    if (command.getNotes() != null) {
                        debt.setNotes(command.getNotes());
                    }
                    return debtRepositoryPort.update(debt);
                });
    }

}
