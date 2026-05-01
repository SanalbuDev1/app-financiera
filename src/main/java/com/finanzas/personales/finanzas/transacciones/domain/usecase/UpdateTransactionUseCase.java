package com.finanzas.personales.finanzas.transacciones.domain.usecase;

import com.finanzas.personales.finanzas.transacciones.domain.model.TransactionsDto;
import com.finanzas.personales.finanzas.transacciones.domain.model.port.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Caso de uso para actualizar una transacción financiera existente.
 * Verifica que la transacción exista y pertenezca al usuario antes de actualizarla.
 * La validación de categoría y tipo se resuelve por FK constraint en BD.
 * No tiene anotaciones de Spring — se registra como @Bean en ApplicationConfig.
 */
@RequiredArgsConstructor
public class UpdateTransactionUseCase {

    /** Puerto de salida para operaciones de persistencia. */
    private final TransactionRepositoryPort transactionRepositoryPort;

    /**
     * Ejecuta la actualización de una transacción.
     * Construye el modelo de dominio con los datos actualizados y delega al port.
     *
     * @param transactionId   UUID de la transacción a actualizar
     * @param userId          identificador del usuario (extraído del JWT)
     * @param description     nueva descripción
     * @param amount          nuevo monto
     * @param category        nueva categoría (lowercase, validada por FK en BD)
     * @param type            nuevo tipo ("income" o "expense", validado por FK en BD)
     * @param transactionDate nueva fecha
     * @param notes           nuevas notas opcionales
     * @return {@code Mono<TransactionsDto>} con la transacción actualizada, o vacío si no existe/no pertenece al usuario
     */
    public Mono<TransactionsDto> execute(String transactionId, String userId, String description,
                                          BigDecimal amount, String category, String type,
                                          LocalDate transactionDate, String notes) {
        // Construir el modelo de dominio con los datos actualizados
        TransactionsDto transaction = TransactionsDto.builder()
                .id(transactionId)
                .userId(userId)
                .description(description)
                .amount(amount)
                .category(category.toLowerCase())
                .type(type.toLowerCase())
                .transactionDate(transactionDate)
                .notes(notes)
                .build();

        return transactionRepositoryPort.update(transaction)
                .flatMap(rowsUpdated -> {
                    if (rowsUpdated > 0) {
                        // Retornar la transacción actualizada con todos los campos resueltos
                        return transactionRepositoryPort.findById(transactionId);
                    }
                    return Mono.empty();
                });
    }
}
