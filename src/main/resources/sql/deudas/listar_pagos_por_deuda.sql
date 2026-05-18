-- Lista todos los pagos de una deuda ordenados por fecha descendente.
-- Parámetro: :debtId
SELECT
    id,
    debt_id,
    payment_date,
    total_amount,
    principal_amount,
    interest_amount,
    payment_type,
    extra_payment_strategy,
    notes,
    created_at
FROM debt_payments
WHERE debt_id = :debtId
ORDER BY payment_date DESC, created_at DESC
