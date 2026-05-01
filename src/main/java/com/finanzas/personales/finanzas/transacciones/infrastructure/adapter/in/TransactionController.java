package com.finanzas.personales.finanzas.transacciones.infrastructure.adapter.in;

import com.finanzas.personales.finanzas.transacciones.domain.usecase.CreateTransactionUseCase;
import com.finanzas.personales.finanzas.transacciones.domain.usecase.DeleteTransactionUseCase;
import com.finanzas.personales.finanzas.transacciones.domain.usecase.GetAllTransactionsUseCase;
import com.finanzas.personales.finanzas.transacciones.domain.usecase.GetSummaryUseCase;
import com.finanzas.personales.finanzas.transacciones.domain.usecase.ListTransactionsUseCase;
import com.finanzas.personales.finanzas.transacciones.domain.usecase.UpdateTransactionUseCase;
import com.finanzas.personales.finanzas.transacciones.infrastructure.dto.TransactionPageResponse;
import com.finanzas.personales.finanzas.transacciones.infrastructure.dto.TransactionRequest;
import com.finanzas.personales.finanzas.transacciones.infrastructure.dto.TransactionResponse;
import com.finanzas.personales.finanzas.transacciones.infrastructure.dto.TransactionSummaryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.stream.Collectors;

/**
 * Adaptador de entrada (input adapter) — Controlador REST para transacciones.
 * Expone los endpoints bajo {@code /api/transactions} y delega la lógica a los use cases.
 * Todos los endpoints requieren JWT válido; el userId se extrae del principal.
 */
@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    /** Caso de uso para listar transacciones paginadas. */
    private final ListTransactionsUseCase listTransactionsUseCase;

    /** Caso de uso para crear una transacción. */
    private final CreateTransactionUseCase createTransactionUseCase;

    /** Caso de uso para eliminar una transacción. */
    private final DeleteTransactionUseCase deleteTransactionUseCase;

    /** Caso de uso para obtener el resumen financiero. */
    private final GetSummaryUseCase getSummaryUseCase;

    /** Caso de uso para obtener todas las transacciones de un usuario. */
    private final GetAllTransactionsUseCase getAllTransactionsUseCase;

    /** Caso de uso para actualizar una transacción existente. */
    private final UpdateTransactionUseCase updateTransactionUseCase;

    /**
     * Lista transacciones paginadas con filtros opcionales.
     * GET /api/transactions?from=&to=&type=&category=&page=0&size=15
     *
     * @param userId   extraído del JWT (principal)
     * @param from     fecha inicio (opcional)
     * @param to       fecha fin (opcional)
     * @param type     tipo de transacción (opcional)
     * @param category categoría (opcional)
     * @param page     página (0-indexed, default 0)
     * @param size     tamaño de página (default 15)
     * @return respuesta paginada con transacciones
     */
    @GetMapping
    public Mono<ResponseEntity<TransactionPageResponse<TransactionResponse>>> listTransactions(
            @AuthenticationPrincipal String userId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        log.info("[API] GET /api/transactions - userId: {}, page: {}, size: {}", userId, page, size);

        return listTransactionsUseCase.execute(userId, from, to, type, category, page, size)
                .map(result -> {
                    // Mapear dominio → DTO response
                    var content = result.content().stream()
                            .map(tx -> TransactionResponse.builder()
                                    .id(tx.getId())
                                    .description(tx.getDescription())
                                    .amount(tx.getAmount())
                                    .category(tx.getCategory())
                                    .type(tx.getType())
                                    .transactionDate(tx.getTransactionDate())
                                    .notes(tx.getNotes())
                                    .createdAt(tx.getCreatedAt())
                                    .build())
                            .collect(Collectors.toList());

                    TransactionPageResponse<TransactionResponse> pageResponse = TransactionPageResponse.<TransactionResponse>builder()
                            .content(content)
                            .totalElements(result.totalElements())
                            .totalPages(result.totalPages())
                            .page(result.page())
                            .size(result.size())
                            .build();

                    return ResponseEntity.ok(pageResponse);
                });
    }

    /**
     * Obtiene todas las transacciones del usuario autenticado sin paginación.
     * GET /api/transactions/all
     *
     * @param userId extraído del JWT (principal)
     * @return lista completa de transacciones del usuario
     */
    @GetMapping("/all")
    public Mono<ResponseEntity<java.util.List<TransactionResponse>>> getAllTransactions(
            @AuthenticationPrincipal String userId) {

        log.info("[API] GET /api/transactions/all - userId: {}", userId);

        return getAllTransactionsUseCase.execute(userId)
                .map(tx -> TransactionResponse.builder()
                        .id(tx.getId())
                        .description(tx.getDescription())
                        .amount(tx.getAmount())
                        .category(tx.getCategory())
                        .type(tx.getType())
                        .transactionDate(tx.getTransactionDate())
                        .notes(tx.getNotes())
                        .createdAt(tx.getCreatedAt())
                        .build())
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Obtiene el resumen financiero del mes.
     * GET /api/transactions/summary?month=4&year=2026
     *
     * @param userId extraído del JWT
     * @param month  mes (1-12)
     * @param year   año
     * @return resumen financiero
     */
    @GetMapping("/summary")
    public Mono<ResponseEntity<TransactionSummaryResponse>> getSummary(
            @AuthenticationPrincipal String userId,
            @RequestParam int month,
            @RequestParam int year) {

        log.info("[API] GET /api/transactions/summary - userId: {}, month: {}/{}", userId, month, year);

        return getSummaryUseCase.execute(userId, month, year)
                .map(result -> {
                    TransactionSummaryResponse response = TransactionSummaryResponse.builder()
                            .totalBalance(result.totalBalance())
                            .monthlyIncome(result.monthlyIncome())
                            .monthlyExpenses(result.monthlyExpenses())
                            .monthlySavings(result.monthlySavings())
                            .savingsGoal(result.savingsGoal())
                            .build();
                    return ResponseEntity.ok(response);
                });
    }

    /**
     * Crea una nueva transacción.
     * POST /api/transactions
     *
     * @param userId  extraído del JWT
     * @param request datos de la transacción validados
     * @return transacción creada con status 201
     */
    @PostMapping
    public Mono<ResponseEntity<TransactionResponse>> createTransaction(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody TransactionRequest request) {

        log.info("[API] POST /api/transactions - userId: {}, description: {}", userId, request.getDescription());

        return createTransactionUseCase.execute(
                userId,
                request.getDescription(),
                request.getAmount(),
                request.getCategory(),
                request.getType(),
                request.getTransactionDate(),
                request.getNotes()
        ).map(tx -> {
            TransactionResponse response = TransactionResponse.builder()
                    .id(tx.getId())
                    .description(tx.getDescription())
                    .amount(tx.getAmount())
                    .category(tx.getCategory())
                    .type(tx.getType())
                    .transactionDate(tx.getTransactionDate())
                    .notes(tx.getNotes())
                    .createdAt(tx.getCreatedAt())
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        });
    }

    /**
     * Actualiza una transacción existente.
     * Solo el dueño (userId del JWT) puede actualizarla.
     * PUT /api/transactions/{id}
     *
     * @param userId  extraído del JWT
     * @param id      UUID de la transacción a actualizar
     * @param request datos actualizados de la transacción validados
     * @return transacción actualizada con status 200, o 404 si no existe/no pertenece al usuario
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<TransactionResponse>> updateTransaction(
            @AuthenticationPrincipal String userId,
            @PathVariable String id,
            @Valid @RequestBody TransactionRequest request) {

        log.info("[API] PUT /api/transactions/{} - userId: {}", id, userId);

        return updateTransactionUseCase.execute(
                id,
                userId,
                request.getDescription(),
                request.getAmount(),
                request.getCategory(),
                request.getType(),
                request.getTransactionDate(),
                request.getNotes()
        ).map(tx -> {
            TransactionResponse response = TransactionResponse.builder()
                    .id(tx.getId())
                    .description(tx.getDescription())
                    .amount(tx.getAmount())
                    .category(tx.getCategory())
                    .type(tx.getType())
                    .transactionDate(tx.getTransactionDate())
                    .notes(tx.getNotes())
                    .createdAt(tx.getCreatedAt())
                    .build();
            return ResponseEntity.ok(response);
        }).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Elimina una transacción por su ID.
     * Solo el dueño (userId del JWT) puede eliminarla.
     * DELETE /api/transactions/{id}
     *
     * @param userId extraído del JWT
     * @param id     UUID de la transacción
     * @return 204 si se eliminó, 404 si no existe o no pertenece al usuario
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteTransaction(
            @AuthenticationPrincipal String userId,
            @PathVariable String id) {

        log.info("[API] DELETE /api/transactions/{} - userId: {}", id, userId);

        return deleteTransactionUseCase.execute(id, userId)
                .map(rowsDeleted -> {
                    if (rowsDeleted > 0) {
                        return ResponseEntity.noContent().<Void>build();
                    } else {
                        return ResponseEntity.notFound().<Void>build();
                    }
                });
    }
}
