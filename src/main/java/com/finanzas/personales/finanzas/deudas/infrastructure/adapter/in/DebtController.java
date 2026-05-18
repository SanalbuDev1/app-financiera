package com.finanzas.personales.finanzas.deudas.infrastructure.adapter.in;

import com.finanzas.personales.finanzas.deudas.domain.model.CreateDebtCommand;
import com.finanzas.personales.finanzas.deudas.domain.model.UpdateDebtCommand;
import com.finanzas.personales.finanzas.deudas.domain.model.Debt;
import com.finanzas.personales.finanzas.deudas.domain.model.DebtDetail;
import com.finanzas.personales.finanzas.deudas.domain.usecase.CreateDebtUseCase;
import com.finanzas.personales.finanzas.deudas.domain.usecase.DeleteDebtUseCase;
import com.finanzas.personales.finanzas.deudas.domain.usecase.GetDebtDetailUseCase;
import com.finanzas.personales.finanzas.deudas.domain.usecase.GetDebtSummaryUseCase;
import com.finanzas.personales.finanzas.deudas.domain.usecase.ListDebtsUseCase;
import com.finanzas.personales.finanzas.deudas.domain.usecase.UpdateDebtUseCase;
import com.finanzas.personales.finanzas.deudas.infrastructure.dto.DebtRequest;
import com.finanzas.personales.finanzas.deudas.infrastructure.dto.DebtResponse;
import com.finanzas.personales.finanzas.deudas.infrastructure.dto.DebtScheduleResponse;
import com.finanzas.personales.finanzas.deudas.infrastructure.dto.DebtSummaryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de entrada (input adapter) — Controlador REST para deudas.
 * Expone los endpoints bajo {@code /api/debts} y delega la lógica a los use cases.
 * Todos los endpoints requieren JWT válido; el userId se extrae del principal
 * mediante {@code @AuthenticationPrincipal} (retorna userId según convención del filtro JWT).
 */
@Slf4j
@RestController
@RequestMapping("/api/debts")
@RequiredArgsConstructor
public class DebtController {

    private final CreateDebtUseCase createDebtUseCase;
    private final ListDebtsUseCase listDebtsUseCase;
    private final GetDebtDetailUseCase getDebtDetailUseCase;
    private final UpdateDebtUseCase updateDebtUseCase;
    private final DeleteDebtUseCase deleteDebtUseCase;
    private final GetDebtSummaryUseCase getDebtSummaryUseCase;

    /**
     * Lista todas las deudas del usuario autenticado, con filtro opcional por estado.
     * GET /api/debts?status=active
     *
     * @param userId identificador del usuario (extraído del JWT)
     * @param status filtro por estado: active, paid_off, defaulted (opcional)
     * @return lista de deudas mapeadas a DTO
     */
    @GetMapping
    public Mono<ResponseEntity<List<DebtResponse>>> listDebts(
            @AuthenticationPrincipal String userId,
            @RequestParam(name = "status", required = false) String status) {

        log.info("[API] GET /api/debts - userId: {}, status: {}", userId, status);

        return listDebtsUseCase.execute(userId, status)
                .map(this::toResponse)
                .collectList()
                .map(ResponseEntity::ok);
    }

    /**
     * Obtiene el detalle de una deuda incluyendo su tabla de amortización.
     * GET /api/debts/{id}
     *
     * @param userId identificador del usuario (extraído del JWT)
     * @param id     identificador de la deuda
     * @return detalle de la deuda con cronograma, o 404 si no existe
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Object>> getDebtDetail(
            @AuthenticationPrincipal String userId,
            @PathVariable("id") String id) {

        log.info("[API] GET /api/debts/{} - userId: {}", id, userId);

        return getDebtDetailUseCase.execute(id, userId)
                .map(detail -> {
                    var debtResponse = toResponse(detail.debt());
                    var scheduleResponse = detail.schedule().stream()
                            .map(item -> DebtScheduleResponse.builder()
                                    .id(item.getId())
                                    .debtId(item.getDebtId())
                                    .installmentNumber(item.getInstallmentNumber())
                                    .dueDate(item.getDueDate())
                                    .principalAmount(item.getPrincipalAmount())
                                    .interestAmount(item.getInterestAmount())
                                    .totalAmount(item.getTotalAmount())
                                    .balanceAfter(item.getBalanceAfter())
                                    .status(item.getStatus())
                                    .createdAt(item.getCreatedAt())
                                    .build())
                            .collect(Collectors.toList());

                    // Retorna un objeto anónimo con debt + schedule
                    var responseBody = new java.util.HashMap<String, Object>();
                    responseBody.put("debt", debtResponse);
                    responseBody.put("schedule", scheduleResponse);
                    return ResponseEntity.ok((Object) responseBody);
                })
                .onErrorResume(IllegalArgumentException.class,
                        ex -> Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * Crea una nueva deuda y genera su tabla de amortización.
     * POST /api/debts
     *
     * @param userId  identificador del usuario (extraído del JWT)
     * @param request datos de la deuda validados
     * @return deuda creada con status 201
     */
    @PostMapping
    public Mono<ResponseEntity<DebtResponse>> createDebt(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody DebtRequest request) {

        log.info("[API] POST /api/debts - userId: {}, creditor: {}", userId, request.getCreditor());

        CreateDebtCommand command = CreateDebtCommand.builder()
                .userId(userId)
                .debtTypeId(request.getDebtTypeId())
                .frequencyId(request.getFrequencyId())
                .creditor(request.getCreditor())
                .description(request.getDescription())
                .originalAmount(request.getOriginalAmount())
                .interestRate(request.getInterestRate())
                .interestRateType(request.getInterestRateType())
                .totalInstallments(request.getTotalInstallments())
                .startDate(request.getStartDate())
                .notes(request.getNotes())
                .build();

        return createDebtUseCase.execute(command)
                .map(debt -> ResponseEntity.status(HttpStatus.CREATED).body(toResponse(debt)));
    }

    /**
     * Actualiza los campos editables de una deuda (creditor, description, notes).
     * PUT /api/debts/{id}
     *
     * @param userId  identificador del usuario (extraído del JWT)
     * @param id      identificador de la deuda
     * @param request datos a actualizar
     * @return deuda actualizada o 404 si no existe
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<DebtResponse>> updateDebt(
            @AuthenticationPrincipal String userId,
            @PathVariable("id") String id,
            @RequestBody DebtRequest request) {

        log.info("[API] PUT /api/debts/{} - userId: {}", id, userId);

        UpdateDebtCommand command = UpdateDebtCommand.builder()
                .debtId(id)
                .userId(userId)
                .creditor(request.getCreditor())
                .description(request.getDescription())
                .notes(request.getNotes())
                .build();

        return updateDebtUseCase.execute(command)
                .map(debt -> ResponseEntity.ok(toResponse(debt)))
                .onErrorResume(IllegalArgumentException.class,
                        ex -> Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * Elimina una deuda del usuario (cascade elimina pagos y cronograma).
     * DELETE /api/debts/{id}
     *
     * @param userId identificador del usuario (extraído del JWT)
     * @param id     identificador de la deuda
     * @return 204 No Content si se eliminó, 404 si no existe
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteDebt(
            @AuthenticationPrincipal String userId,
            @PathVariable("id") String id) {

        log.info("[API] DELETE /api/debts/{} - userId: {}", id, userId);

        return deleteDebtUseCase.execute(id, userId)
                .then(Mono.<ResponseEntity<Void>>just(ResponseEntity.<Void>noContent().build()))
                .onErrorResume(IllegalArgumentException.class,
                        ex -> Mono.just(ResponseEntity.<Void>notFound().build()));
    }

    /**
     * Obtiene el resumen financiero de las deudas activas del usuario.
     * GET /api/debts/summary
     *
     * @param userId identificador del usuario (extraído del JWT)
     * @return resumen con totales agregados
     */
    @GetMapping("/summary")
    public Mono<ResponseEntity<DebtSummaryResponse>> getSummary(
            @AuthenticationPrincipal String userId) {

        log.info("[API] GET /api/debts/summary - userId: {}", userId);

        return getDebtSummaryUseCase.execute(userId)
                .map(summary -> ResponseEntity.ok(DebtSummaryResponse.builder()
                        .totalDebts(summary.getTotalDebts())
                        .totalBalance(summary.getTotalBalance())
                        .totalOriginalAmount(summary.getTotalOriginalAmount())
                        .totalMonthlyPayment(summary.getTotalMonthlyPayment())
                        .totalPendingInterest(summary.getTotalPendingInterest())
                        .averageProgress(summary.getAverageProgress())
                        .build()));
    }

    /**
     * Mapea el modelo de dominio {@link Debt} al DTO {@link DebtResponse}.
     * Calcula el porcentaje de progreso: (1 - saldo_actual / monto_original) × 100.
     *
     * @param debt modelo de dominio
     * @return DTO de respuesta
     */
    private DebtResponse toResponse(Debt debt) {
        BigDecimal progress = BigDecimal.ZERO;
        if (debt.getOriginalAmount() != null && debt.getOriginalAmount().compareTo(BigDecimal.ZERO) > 0
                && debt.getCurrentBalance() != null) {
            progress = BigDecimal.ONE
                    .subtract(debt.getCurrentBalance().divide(debt.getOriginalAmount(), 4, RoundingMode.HALF_UP))
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return DebtResponse.builder()
                .id(debt.getId())
                .userId(debt.getUserId())
                .debtTypeId(debt.getDebtTypeId())
                .debtTypeName(debt.getDebtTypeName())
                .frequencyId(debt.getFrequencyId())
                .frequencyName(debt.getFrequencyName())
                .creditor(debt.getCreditor())
                .description(debt.getDescription())
                .originalAmount(debt.getOriginalAmount())
                .currentBalance(debt.getCurrentBalance())
                .interestRate(debt.getInterestRate())
                .interestRateType(debt.getInterestRateType())
                .totalInstallments(debt.getTotalInstallments())
                .remainingInstallments(debt.getRemainingInstallments())
                .installmentAmount(debt.getInstallmentAmount())
                .startDate(debt.getStartDate())
                .nextPaymentDate(debt.getNextPaymentDate())
                .status(debt.getStatus())
                .notes(debt.getNotes())
                .createdAt(debt.getCreatedAt())
                .progressPercentage(progress)
                .build();
    }
}
