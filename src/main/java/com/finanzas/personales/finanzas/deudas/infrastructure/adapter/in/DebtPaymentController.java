package com.finanzas.personales.finanzas.deudas.infrastructure.adapter.in;

import com.finanzas.personales.finanzas.deudas.domain.model.RegisterPaymentCommand;
import com.finanzas.personales.finanzas.deudas.domain.usecase.GetDebtDetailUseCase;
import com.finanzas.personales.finanzas.deudas.domain.usecase.RegisterPaymentUseCase;
import com.finanzas.personales.finanzas.deudas.infrastructure.dto.DebtPaymentRequest;
import com.finanzas.personales.finanzas.deudas.infrastructure.dto.DebtPaymentResponse;
import com.finanzas.personales.finanzas.deudas.infrastructure.dto.DebtScheduleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adaptador de entrada para pagos y cronograma de deudas.
 * Expone los endpoints bajo {@code /api/debts/{id}/payments} y {@code /api/debts/{id}/schedule}.
 * Todos los endpoints requieren JWT válido; el userId se extrae del principal.
 */
@Slf4j
@RestController
@RequestMapping("/api/debts")
@RequiredArgsConstructor
public class DebtPaymentController {

    private final RegisterPaymentUseCase registerPaymentUseCase;
    private final GetDebtDetailUseCase getDebtDetailUseCase;

    /**
     * Registra un pago sobre una deuda (regular o extraordinario).
     * POST /api/debts/{id}/payments
     *
     * @param userId  identificador del usuario (extraído del JWT)
     * @param id      identificador de la deuda
     * @param request datos del pago validados
     * @return pago registrado con status 201, o 404 si la deuda no existe
     */
    @PostMapping("/{id}/payments")
    public Mono<ResponseEntity<DebtPaymentResponse>> registerPayment(
            @AuthenticationPrincipal String userId,
            @PathVariable("id") String id,
            @Valid @RequestBody DebtPaymentRequest request) {

        log.info("[API] POST /api/debts/{}/payments - userId: {}, type: {}", id, userId, request.getPaymentType());

        RegisterPaymentCommand command = RegisterPaymentCommand.builder()
                .debtId(id)
                .userId(userId)
                .paymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now())
                .totalAmount(request.getTotalAmount())
                .paymentType(request.getPaymentType())
                .extraPaymentStrategy(request.getExtraPaymentStrategy())
                .notes(request.getNotes())
                .build();

        return registerPaymentUseCase.execute(command)
                .map(payment -> ResponseEntity.status(HttpStatus.CREATED).body(
                        DebtPaymentResponse.builder()
                                .id(payment.getId())
                                .debtId(payment.getDebtId())
                                .paymentDate(payment.getPaymentDate())
                                .totalAmount(payment.getTotalAmount())
                                .principalAmount(payment.getPrincipalAmount())
                                .interestAmount(payment.getInterestAmount())
                                .paymentType(payment.getPaymentType())
                                .extraPaymentStrategy(payment.getExtraPaymentStrategy())
                                .notes(payment.getNotes())
                                .createdAt(payment.getCreatedAt())
                                .build()))
                .onErrorResume(IllegalArgumentException.class,
                        ex -> Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * Obtiene la tabla de amortización (cronograma) de una deuda.
     * GET /api/debts/{id}/schedule
     *
     * @param userId identificador del usuario (extraído del JWT)
     * @param id     identificador de la deuda
     * @return lista de ítems del cronograma, o 404 si la deuda no existe
     */
    @GetMapping("/{id}/schedule")
    public Mono<ResponseEntity<List<DebtScheduleResponse>>> getSchedule(
            @AuthenticationPrincipal String userId,
            @PathVariable("id") String id) {

        log.info("[API] GET /api/debts/{}/schedule - userId: {}", id, userId);

        return getDebtDetailUseCase.execute(id, userId)
                .map(detail -> {
                    List<DebtScheduleResponse> schedule = detail.schedule().stream()
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
                    return ResponseEntity.ok(schedule);
                })
                .onErrorResume(IllegalArgumentException.class,
                        ex -> Mono.just(ResponseEntity.notFound().build()));
    }
}
