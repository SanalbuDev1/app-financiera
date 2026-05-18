package com.finanzas.personales.finanzas.deudas.domain.usecase;

import com.finanzas.personales.finanzas.deudas.domain.model.AmortizationResult;
import com.finanzas.personales.finanzas.deudas.domain.model.CreateDebtCommand;
import com.finanzas.personales.finanzas.deudas.domain.model.Debt;
import com.finanzas.personales.finanzas.deudas.domain.model.DebtScheduleItem;
import com.finanzas.personales.finanzas.deudas.domain.model.DebtType;
import com.finanzas.personales.finanzas.deudas.domain.model.PaymentFrequency;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtRepositoryPort;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtScheduleRepositoryPort;
import com.finanzas.personales.finanzas.deudas.domain.port.DebtTypeRepositoryPort;
import com.finanzas.personales.finanzas.deudas.domain.port.PaymentFrequencyRepositoryPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Caso de uso para crear una nueva deuda.
 * Valida los datos, calcula la tabla de amortización y persiste la deuda.
 */
@RequiredArgsConstructor
public class CreateDebtUseCase {

    private final DebtRepositoryPort debtRepositoryPort;
    private final DebtScheduleRepositoryPort debtScheduleRepositoryPort;
    private final DebtTypeRepositoryPort debtTypeRepositoryPort;
    private final PaymentFrequencyRepositoryPort paymentFrequencyRepositoryPort;
    private final CalculateAmortizationUseCase calculateAmortizationUseCase;

    /**
     * Crea una nueva deuda con su tabla de amortización.
     *
     * @param command datos para crear la deuda
     * @return {@code Mono<Debt>} con la deuda creada
     */
    public Mono<Debt> execute(CreateDebtCommand command) {
        // Validar datos básicos primero (síncrono)
        return validateCommand(command)
                // Obtener tipo de deuda (defer para evaluación lazy)
                .then(Mono.defer(() -> debtTypeRepositoryPort.findById(command.getDebtTypeId())))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Tipo de deuda no encontrado: " + command.getDebtTypeId())))
                // Obtener frecuencia de pago (después de verificar que existe el tipo)
                .flatMap(debtType -> Mono.defer(() -> paymentFrequencyRepositoryPort.findById(command.getFrequencyId()))
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Frecuencia de pago no encontrada: " + command.getFrequencyId())))
                        .map(frequency -> new DebtTypeAndFrequency(debtType, frequency)))
                // Calcular amortización y crear deuda
                .flatMap(data -> {
                    DebtType debtType = data.debtType;
                    PaymentFrequency frequency = data.frequency;

                    // Calcular tabla de amortización
                    AmortizationResult amortization = calculateAmortizationUseCase.execute(
                            command.getOriginalAmount(),
                            command.getInterestRate(),
                            command.getInterestRateType(),
                            command.getTotalInstallments(),
                            frequency.getName(),
                            command.getStartDate()
                    );

                    // Crear la deuda
                    Debt debt = buildDebt(command, debtType, frequency, amortization);

                    // Guardar deuda
                    return debtRepositoryPort.save(debt)
                            .flatMap(savedDebt -> {
                                // Asignar el debtId a cada ítem del schedule
                                List<DebtScheduleItem> scheduleWithDebtId = amortization.getSchedule().stream()
                                        .map(item -> DebtScheduleItem.builder()
                                                .id(item.getId())
                                                .debtId(savedDebt.getId())
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

                                // Guardar schedule
                                return debtScheduleRepositoryPort.saveAll(scheduleWithDebtId)
                                        .then(Mono.just(savedDebt));
                            });
                });
    }

    /**
     * Clase auxiliar para transportar tipo de deuda y frecuencia juntos.
     */
    private record DebtTypeAndFrequency(DebtType debtType, PaymentFrequency frequency) {}

    /**
     * Valida los datos del comando.
     */
    private Mono<Void> validateCommand(CreateDebtCommand command) {
        if (command.getOriginalAmount() == null || command.getOriginalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new IllegalArgumentException("El monto original debe ser mayor a 0"));
        }
        if (command.getTotalInstallments() == null || command.getTotalInstallments() <= 0) {
            return Mono.error(new IllegalArgumentException("El número de cuotas debe ser mayor a 0"));
        }
        if (command.getInterestRate() == null || command.getInterestRate().compareTo(BigDecimal.ZERO) < 0) {
            return Mono.error(new IllegalArgumentException("La tasa de interés no puede ser negativa"));
        }
        if (command.getUserId() == null || command.getUserId().isBlank()) {
            return Mono.error(new IllegalArgumentException("El usuario es requerido"));
        }
        if (command.getCreditor() == null || command.getCreditor().isBlank()) {
            return Mono.error(new IllegalArgumentException("El acreedor es requerido"));
        }
        return Mono.empty();
    }

    /**
     * Construye el objeto Debt a partir del comando y los datos calculados.
     */
    private Debt buildDebt(CreateDebtCommand command, DebtType debtType,
                           PaymentFrequency frequency, AmortizationResult amortization) {

        // Calcular próxima fecha de pago (primer ítem del schedule)
        LocalDate nextPaymentDate = amortization.getSchedule().isEmpty()
                ? command.getStartDate().plusMonths(1)
                : amortization.getSchedule().get(0).getDueDate();

        return Debt.builder()
                .id(UUID.randomUUID().toString())
                .userId(command.getUserId())
                .debtTypeId(command.getDebtTypeId())
                .debtTypeName(debtType.getName())
                .frequencyId(command.getFrequencyId())
                .frequencyName(frequency.getName())
                .creditor(command.getCreditor())
                .description(command.getDescription())
                .originalAmount(command.getOriginalAmount())
                .currentBalance(command.getOriginalAmount())
                .interestRate(command.getInterestRate())
                .interestRateType(command.getInterestRateType())
                .totalInstallments(command.getTotalInstallments())
                .remainingInstallments(command.getTotalInstallments())
                .installmentAmount(amortization.getInstallmentAmount())
                .startDate(command.getStartDate())
                .nextPaymentDate(nextPaymentDate)
                .status("active")
                .notes(command.getNotes())
                .createdAt(LocalDateTime.now())
                .build();
    }

}
