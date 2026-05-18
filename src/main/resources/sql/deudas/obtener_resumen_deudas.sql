-- Obtiene el resumen agregado de las deudas activas de un usuario.
-- Calcula: cantidad de deudas activas, saldo total, monto original total,
-- suma de cuotas mensuales, intereses pendientes estimados y porcentaje de avance.
-- Parámetro: :userId
SELECT
    COUNT(*)                                            AS total_debts,
    COALESCE(SUM(d.current_balance), 0)                AS total_balance,
    COALESCE(SUM(d.original_amount), 0)                AS total_original_amount,
    COALESCE(SUM(d.installment_amount), 0)             AS total_monthly_payment,
    -- Intereses pendientes estimados: cuota × cuotas_restantes − saldo_actual
    COALESCE(
        SUM((d.installment_amount * d.remaining_installments) - d.current_balance),
        0
    )                                                   AS total_pending_interest,
    -- Porcentaje de avance: capital pagado / original × 100
    COALESCE(
        (1 - SUM(d.current_balance) / NULLIF(SUM(d.original_amount), 0)) * 100,
        0
    )                                                   AS average_progress
FROM debts d
WHERE d.user_id = :userId
  AND d.status = 'active'
