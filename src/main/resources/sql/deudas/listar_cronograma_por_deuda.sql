-- Obtiene el cronograma completo de una deuda ordenado por número de cuota.
-- Parámetro: :debtId
SELECT
    id,
    debt_id,
    installment_number,
    due_date,
    principal_amount,
    interest_amount,
    total_amount,
    balance_after,
    status,
    created_at
FROM debt_schedule
WHERE debt_id = :debtId
ORDER BY installment_number ASC
