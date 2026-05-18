package com.finanzas.personales.finanzas.deudas.domain.model;

import java.util.List;

/**
 * Resultado del caso de uso {@code GetDebtDetailUseCase}.
 * Agrupa la deuda principal con su cronograma completo de cuotas proyectadas.
 *
 * @param debt     datos de la deuda
 * @param schedule lista de cuotas proyectadas ordenadas por número de cuota
 */
public record DebtDetail(Debt debt, List<DebtScheduleItem> schedule) {}
