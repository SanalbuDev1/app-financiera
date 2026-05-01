package com.finanzas.personales.finanzas.transacciones.domain.usecase;

import com.finanzas.personales.finanzas.transacciones.domain.model.TransactionsDto;
import com.finanzas.personales.finanzas.transacciones.domain.model.port.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

/**
 * Caso de uso para listar transacciones con paginación y filtros.
 * Delega al port tanto la consulta paginada como el conteo total.
 * No tiene anotaciones de Spring — se registra como @Bean en ApplicationConfig.
 */
@RequiredArgsConstructor
public class ListTransactionsUseCase {

    /** Puerto de salida para consultar transacciones. */
    private final TransactionRepositoryPort transactionRepositoryPort;

    /**
     * Ejecuta la consulta paginada de transacciones con filtros opcionales.
     *
     * @param userId   identificador del usuario (del JWT)
     * @param from     fecha inicio (inclusive), puede ser null
     * @param to       fecha fin (inclusive), puede ser null
     * @param type     tipo de transacción, puede ser null
     * @param category categoría, puede ser null
     * @param page     número de página (0-indexed)
     * @param size     tamaño de página
     * @return {@code Mono<PageResult>} con los resultados y metadatos de paginación
     */
    public Mono<PageResult> execute(String userId, LocalDate from, LocalDate to,
                                     String type, String category, int page, int size) {
        int offset = page * size;

        // Ejecutar consulta paginada y conteo en paralelo
        return Mono.zip(
                transactionRepositoryPort.findByUserIdPaginated(userId, from, to, type, category, offset, size)
                        .collectList(),
                transactionRepositoryPort.countByUserIdFiltered(userId, from, to, type, category)
        ).map(tuple -> {
            List<TransactionsDto> content = tuple.getT1();
            long totalElements = tuple.getT2();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            return new PageResult(content, totalElements, totalPages, page, size);
        });
    }

    /**
     * Resultado paginado con metadatos.
     *
     * @param content       lista de transacciones de la página
     * @param totalElements total de registros
     * @param totalPages    total de páginas
     * @param page          página actual
     * @param size          tamaño de página
     */
    public record PageResult(
            List<TransactionsDto> content,
            long totalElements,
            int totalPages,
            int page,
            int size
    ) {}
}
