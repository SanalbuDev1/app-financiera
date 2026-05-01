package com.finanzas.personales.finanzas.transacciones.domain.usecase;

import com.finanzas.personales.finanzas.transacciones.domain.model.TransactionsDto;
import com.finanzas.personales.finanzas.transacciones.domain.model.port.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Caso de uso para crear una nueva transacción financiera.
 * Genera el UUID y delega al port de persistencia.
 * La validación de categoría y tipo se resuelve por FK constraint en BD.
 * No tiene anotaciones de Spring — se registra como @Bean en ApplicationConfig.
 */
@RequiredArgsConstructor
public class CreateTransactionUseCase {

    /** Puerto de salida para persistir transacciones. */
    private final TransactionRepositoryPort transactionRepositoryPort;

    /**
     * Ejecuta la creación de una transacción.
     *
     * @param userId      identificador del usuario (extraído del JWT)
     * @param description descripción de la transacción
     * @param amount      monto (positivo)
     * @param category    nombre de la categoría (lowercase, validada por FK en BD)
     * @param type        nombre del tipo ("income" o "expense", validado por FK en BD)
     * @param transactionDate fecha de la transacción
     * @param notes       notas opcionales
     * @return {@code Mono<TransactionsDto>} con la transacción creada
     */
    public Mono<TransactionsDto> execute(String userId, String description, java.math.BigDecimal amount,
                                          String category, String type, java.time.LocalDate transactionDate,
                                          String notes) {
        // Construir el modelo de dominio con UUID y timestamp generados
        TransactionsDto transaction = TransactionsDto.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .description(description)
                .amount(amount)
                .category(category.toLowerCase())
                .type(type.toLowerCase())
                .transactionDate(transactionDate)
                .notes(notes)
                .createdAt(LocalDateTime.now())
                .build();

        return transactionRepositoryPort.save(transaction);
    }
}
