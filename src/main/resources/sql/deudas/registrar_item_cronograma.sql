-- Inserta múltiples ítems de cronograma para una deuda.
-- Se ejecuta una vez por ítem con los parámetros:
-- :id, :debtId, :installmentNumber, :dueDate, :principalAmount,
-- :interestAmount, :totalAmount, :balanceAfter, :status, :createdAt
INSERT INTO debt_schedule (
    id, debt_id, installment_number, due_date,
    principal_amount, interest_amount, total_amount,
    balance_after, status, created_at
) VALUES (
    :id, :debtId, :installmentNumber, :dueDate,
    :principalAmount, :interestAmount, :totalAmount,
    :balanceAfter, :status, :createdAt
)
