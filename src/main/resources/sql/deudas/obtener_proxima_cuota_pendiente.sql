-- Obtiene la próxima cuota pendiente (menor installment_number con status='pending').
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
  AND status = 'pending'
ORDER BY installment_number ASC
LIMIT 1
