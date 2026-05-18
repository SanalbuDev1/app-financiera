-- Obtiene una deuda por su ID haciendo JOIN con debt_types y payment_frequencies
-- para resolver los nombres de tipo y frecuencia.
-- Parámetro: :id
SELECT
    d.id,
    d.user_id,
    d.debt_type_id,
    dt.name             AS debt_type_name,
    d.frequency_id,
    pf.name             AS frequency_name,
    d.creditor,
    d.description,
    d.original_amount,
    d.current_balance,
    d.interest_rate,
    d.interest_rate_type,
    d.total_installments,
    d.remaining_installments,
    d.installment_amount,
    d.start_date,
    d.next_payment_date,
    d.status,
    d.notes,
    d.created_at
FROM debts d
JOIN debt_types dt          ON dt.id = d.debt_type_id
JOIN payment_frequencies pf ON pf.id = d.frequency_id
WHERE d.id = :id
